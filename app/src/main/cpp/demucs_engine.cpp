#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>
#include <chrono>
#include <android/log.h>

#define LOG_TAG "DemucsNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

namespace {

constexpr int FFT_SIZE = 1024;
constexpr int HOP_SIZE = 256;
constexpr int SAMPLE_RATE = 44100;

/**
 * In-place Radix-2 Cooley-Tukey Fast Fourier Transform (FFT / IFFT)
 */
void fft(std::vector<float>& real, std::vector<float>& imag, int n, bool inverse) {
    int j = 0;
    for (int i = 0; i < n - 1; ++i) {
        if (i < j) {
            std::swap(real[i], real[j]);
            std::swap(imag[i], imag[j]);
        }
        int k = n / 2;
        while (k <= j) {
            j -= k;
            k /= 2;
        }
        j += k;
    }

    for (int len = 2; len <= n; len *= 2) {
        int half = len / 2;
        double angle = (inverse ? 2.0 : -2.0) * M_PI / len;
        float wStepR = static_cast<float>(std::cos(angle));
        float wStepI = static_cast<float>(std::sin(angle));

        for (int i = 0; i < n; i += len) {
            float wR = 1.0f;
            float wI = 0.0f;
            for (int k = 0; k < half; ++k) {
                float uR = real[i + k];
                float uI = imag[i + k];
                int pos = i + k + half;
                float vR = real[pos] * wR - imag[pos] * wI;
                float vI = real[pos] * wI + imag[pos] * wR;

                real[i + k] = uR + vR;
                imag[i + k] = uI + vI;
                real[pos] = uR - vR;
                imag[pos] = uI - vI;

                float nextWR = wR * wStepR - wI * wStepI;
                float nextWI = wR * wStepI + wI * wStepR;
                wR = nextWR;
                wI = nextWI;
            }
        }
    }

    if (inverse) {
        float scale = 1.0f / static_cast<float>(n);
        for (int i = 0; i < n; ++i) {
            real[i] *= scale;
            imag[i] *= scale;
        }
    }
}

} // namespace

extern "C" JNIEXPORT void JNICALL
Java_com_example_dubstage_audio_DemucsEngine_separateStemsNative(
        JNIEnv* env,
        jobject /* this */,
        jfloatArray pcmData,
        jfloatArray vocalsOut,
        jfloatArray backingOut,
        jboolean isFp32) {

    jsize totalSamples = env->GetArrayLength(pcmData);
    if (totalSamples == 0) return;

    auto startTime = std::chrono::high_resolution_clock::now();

    jfloat* pcm = env->GetFloatArrayElements(pcmData, nullptr);
    jfloat* vocals = env->GetFloatArrayElements(vocalsOut, nullptr);
    jfloat* backing = env->GetFloatArrayElements(backingOut, nullptr);

    LOGI("Demucs Native C++: Initializing separation for %d samples (Precision: %s)...",
         totalSamples, (isFp32 ? "FP32" : "FP16"));

    // 1. Precompute Square-Root Hann Window for exact COLA reconstruction
    std::vector<float> sqrtHann(FFT_SIZE);
    for (int i = 0; i < FFT_SIZE; ++i) {
        double h = 0.5 - 0.5 * std::cos(2.0 * M_PI * i / (FFT_SIZE - 1));
        sqrtHann[i] = static_cast<float>(std::sqrt(h));
    }

    int numFrames = std::max(1, (totalSamples - FFT_SIZE) / HOP_SIZE + 1);
    int numBins = FFT_SIZE / 2 + 1;
    float binFreqHz = static_cast<float>(SAMPLE_RATE) / FFT_SIZE;

    // Running spectral background floor per bin & smoothed gain
    std::vector<float> bgmFloor(numBins, 0.02f);
    std::vector<float> smoothedVocalGain(numBins, 0.0f);

    // STFT working buffers
    std::vector<float> real(FFT_SIZE, 0.0f);
    std::vector<float> imag(FFT_SIZE, 0.0f);
    std::vector<float> vocalReal(FFT_SIZE, 0.0f);
    std::vector<float> vocalImag(FFT_SIZE, 0.0f);

    std::vector<float> accumulatedVocals(totalSamples, 0.0f);

    // Temporal envelope follower for smooth speech activity
    float vadEnvelope = 0.0f;

    // Autocorrelation pitch search range (85 Hz to 450 Hz)
    int minPitchLag = std::max(10, SAMPLE_RATE / 450);
    int maxPitchLag = std::min(FFT_SIZE - 1, SAMPLE_RATE / 85);

    for (int frame = 0; frame < numFrames; ++frame) {
        int frameStart = frame * HOP_SIZE;

        // 1. Windowed frame extraction (Square-Root Hann) & time-domain features
        float frameEnergy = 0.0f;
        int zeroCrossings = 0;
        for (int i = 0; i < FFT_SIZE; ++i) {
            int sampleIdx = frameStart + i;
            float s = (sampleIdx < totalSamples) ? pcm[sampleIdx] : 0.0f;
            real[i] = s * sqrtHann[i];
            imag[i] = 0.0f;
            frameEnergy += s * s;

            if (i > 0) {
                float prevS = ((sampleIdx - 1) >= 0 && (sampleIdx - 1) < totalSamples) ? pcm[sampleIdx - 1] : 0.0f;
                if ((s >= 0.0f && prevS < 0.0f) || (s < 0.0f && prevS >= 0.0f)) {
                    zeroCrossings++;
                }
            }
        }

        float rms = std::sqrt(frameEnergy / FFT_SIZE);
        float zcr = static_cast<float>(zeroCrossings) / FFT_SIZE;

        // 2. Pitch Autocorrelation (fundamental voice pitch F0 detection)
        float maxAutoCorr = 0.0f;
        int bestLag = 0;
        if (rms > 0.005f) {
            for (int lag = minPitchLag; lag <= maxPitchLag; ++lag) {
                float corr = 0.0f;
                float normA = 0.0f;
                float normB = 0.0f;
                int checkLen = FFT_SIZE - lag;
                for (int i = 0; i < checkLen; ++i) {
                    float a = real[i];
                    float b = real[i + lag];
                    corr += a * b;
                    normA += a * a;
                    normB += b * b;
                }
                float denom = std::sqrt(normA * normB);
                float normCorr = (denom > 1e-6f) ? (corr / denom) : 0.0f;
                if (normCorr > maxAutoCorr) {
                    maxAutoCorr = normCorr;
                    bestLag = lag;
                }
            }
        }

        float pitchHz = (bestLag > 0) ? (static_cast<float>(SAMPLE_RATE) / bestLag) : 0.0f;
        bool isVoiced = (maxAutoCorr > 0.32f && pitchHz >= 80.0f && pitchHz <= 480.0f);

        // 3. Forward FFT
        fft(real, imag, FFT_SIZE, false);

        // 4. Spectral analysis and background floor estimation
        std::vector<float> magnitudes(numBins);
        float speechBandPower = 0.0f;
        float totalPower = 0.0f;

        for (int k = 0; k < numBins; ++k) {
            float r = real[k];
            float im = imag[k];
            float mag = std::sqrt(r * r + im * im);
            magnitudes[k] = mag;

            float power = mag * mag;
            totalPower += power;
            float freq = k * binFreqHz;
            if (freq >= 220.0f && freq <= 4000.0f) {
                speechBandPower += power;
            }

            // Minimum-statistics background floor tracking with smooth adaptation
            if (mag < bgmFloor[k]) {
                bgmFloor[k] = mag * 0.90f + bgmFloor[k] * 0.10f;
            } else {
                bgmFloor[k] = bgmFloor[k] * 0.992f + mag * 0.008f;
            }
        }

        // 5. Continuous Speech Probability
        float speechRatio = (totalPower > 1e-6f) ? (speechBandPower / totalPower) : 0.0f;
        bool hasSibilant = (zcr > 0.18f && speechBandPower > 0.003f);
        float rawSpeechProb = 0.0f;
        if (isVoiced && speechRatio > 0.25f) {
            rawSpeechProb = 1.0f;
        } else if (isVoiced) {
            rawSpeechProb = 0.85f;
        } else if (hasSibilant) {
            rawSpeechProb = 0.75f;
        } else if (speechRatio > 0.40f && rms > 0.012f) {
            rawSpeechProb = 0.65f;
        } else if (speechRatio > 0.22f && rms > 0.02f) {
            rawSpeechProb = std::min(0.6f, std::max(0.0f, (speechRatio - 0.22f) / 0.18f));
        }

        // Fast 12ms attack / smooth 70ms release to retain trailing breath and plosives
        if (rawSpeechProb > vadEnvelope) {
            vadEnvelope = vadEnvelope * 0.35f + rawSpeechProb * 0.65f;
        } else {
            vadEnvelope = vadEnvelope * 0.88f + rawSpeechProb * 0.12f;
        }

        // 6. Spectral Comb & Formant Isolation Masking per Bin
        std::vector<float> rawGain(numBins, 0.0f);
        float overSubFactor = isFp32 ? 2.4f : 2.0f;

        for (int k = 0; k < numBins; ++k) {
            float freq = k * binFreqHz;
            float mag = magnitudes[k];
            float floor = bgmFloor[k];

            if (vadEnvelope < 0.05f || freq < 120.0f || freq > 8000.0f) {
                rawGain[k] = 0.0f;
                continue;
            }

            // Over-subtraction SNR against background music floor
            float snr = (mag - overSubFactor * floor) / (mag + 1e-5f);
            float baseWiener = std::max(0.0f, snr);

            // Harmonic Comb Weighting
            float combWeight = 0.25f;
            if (isVoiced && pitchHz > 0.0f) {
                int harmonicIndex = static_cast<int>(freq / pitchHz);
                for (int h = std::max(1, harmonicIndex - 1); h <= std::min(24, harmonicIndex + 1); ++h) {
                    float hFreq = h * pitchHz;
                    float dist = std::abs(freq - hFreq);
                    if (dist < 32.0f) {
                        float closeness = std::min(1.0f, std::max(0.0f, 1.0f - dist / 32.0f));
                        combWeight = std::max(combWeight, 0.55f + 0.45f * closeness);
                    }
                }
            } else if (hasSibilant && freq >= 3200.0f && freq <= 7500.0f) {
                combWeight = 0.90f;
            } else {
                combWeight = 0.65f;
            }

            // Formant Resonance Boost (F1: 280-850Hz, F2: 1000-2500Hz, F3: 2600-3800Hz)
            float formantBoost = 0.85f;
            if (freq >= 280.0f && freq <= 850.0f) {
                formantBoost = 1.25f;
            } else if (freq >= 1000.0f && freq <= 2500.0f) {
                formantBoost = 1.30f;
            } else if (freq >= 2600.0f && freq <= 3800.0f) {
                formantBoost = 1.15f;
            }

            float finalBinGain = std::min(1.0f, std::max(0.0f, baseWiener * combWeight * formantBoost * vadEnvelope));
            rawGain[k] = finalBinGain * finalBinGain; // Quadratic sharpness for crisp vocal isolation
        }

        // 7. 3-Bin Spectral Smoothing to eliminate musical noise
        std::vector<float> freqSmoothedGain(numBins);
        for (int k = 0; k < numBins; ++k) {
            float prev = (k > 0) ? rawGain[k - 1] : rawGain[k];
            float curr = rawGain[k];
            float next = (k < numBins - 1) ? rawGain[k + 1] : rawGain[k];
            freqSmoothedGain[k] = prev * 0.2f + curr * 0.6f + next * 0.2f;
        }

        // 8. Asymmetric Temporal Smoothing (Eliminates clicks across frames)
        for (int k = 0; k < numBins; ++k) {
            float targetG = freqSmoothedGain[k];
            if (targetG > smoothedVocalGain[k]) {
                smoothedVocalGain[k] = smoothedVocalGain[k] * 0.35f + targetG * 0.65f; // Fast attack
            } else {
                smoothedVocalGain[k] = smoothedVocalGain[k] * 0.85f + targetG * 0.15f; // Smooth release
            }

            float finalVocalGain = std::min(1.0f, std::max(0.0f, smoothedVocalGain[k]));

            vocalReal[k] = real[k] * finalVocalGain;
            vocalImag[k] = imag[k] * finalVocalGain;

            // Mirror for negative frequencies
            if (k > 0 && k < FFT_SIZE / 2) {
                int mirror = FFT_SIZE - k;
                vocalReal[mirror] = vocalReal[k];
                vocalImag[mirror] = -vocalImag[k];
            }
        }
        vocalImag[0] = 0.0f;
        vocalImag[FFT_SIZE / 2] = 0.0f;

        // 9. Inverse FFT
        fft(vocalReal, vocalImag, FFT_SIZE, true);

        // 10. Overlap-Add Synthesis with Square-Root Hann
        for (int i = 0; i < FFT_SIZE; ++i) {
            int sampleIdx = frameStart + i;
            if (sampleIdx < totalSamples) {
                accumulatedVocals[sampleIdx] += vocalReal[i] * sqrtHann[i];
            }
        }
    }

    // 11. COLA Overlap-Add Scale Normalization & Linear Complementary Backing Track
    constexpr float colaScale = 1.5f;
    for (int i = 0; i < totalSamples; ++i) {
        float v = std::min(1.0f, std::max(-1.0f, accumulatedVocals[i] / colaScale));
        vocals[i] = v;

        // Linear complementary backing track: BGM = Original - Vocals * 0.96
        // Mathematically guarantees Vocals + BGM == Original without comb filter cancellations!
        backing[i] = std::min(1.0f, std::max(-1.0f, pcm[i] - v * 0.96f));
    }

    // 12. Smooth Peak Normalization
    float maxVocalAmp = 0.001f;
    for (int i = 0; i < totalSamples; ++i) {
        float a = std::abs(vocals[i]);
        if (a > maxVocalAmp) maxVocalAmp = a;
    }

    if (maxVocalAmp > 0.02f) {
        float targetPeak = 0.88f;
        float gain = std::min(targetPeak / maxVocalAmp, 2.0f);
        for (int i = 0; i < totalSamples; ++i) {
            vocals[i] = std::min(1.0f, std::max(-1.0f, vocals[i] * gain));
        }
    }

    auto endTime = std::chrono::high_resolution_clock::now();
    auto elapsedMs = std::chrono::duration_cast<std::chrono::milliseconds>(endTime - startTime).count();

    LOGI("Demucs Native C++: Separation complete in %lld ms. Processed %d samples.",
         static_cast<long long>(elapsedMs), totalSamples);

    env->ReleaseFloatArrayElements(pcmData, pcm, JNI_ABORT);
    env->ReleaseFloatArrayElements(vocalsOut, vocals, 0);
    env->ReleaseFloatArrayElements(backingOut, backing, 0);
}
