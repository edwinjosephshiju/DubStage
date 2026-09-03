#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <torch/script.h> // LibTorch C++ API
#include <torch/torch.h>

#define LOG_TAG "DemucsEngineNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_dubstage_audio_DemucsEngine_separateStemsNative(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPathStr,
        jfloatArray pcmData,
        jfloatArray vocalsOut,
        jfloatArray backingOut,
        jboolean isFp32) {

    const char* cModelPath = env->GetStringUTFChars(modelPathStr, nullptr);
    std::string modelPath(cModelPath);
    env->ReleaseStringUTFChars(modelPathStr, cModelPath);

    // 1. Mandatory Neural Weights Validation
    try {
        // Load the TorchScript model
        LOGI("Loading PyTorch Mobile model from: %s", modelPath.c_str());
        torch::jit::script::Module module = torch::jit::load(modelPath);
        LOGI("PyTorch model loaded successfully.");
        
        // Prepare input tensors
        jsize numSamples = env->GetArrayLength(pcmData);
        jfloat* inData = env->GetFloatArrayElements(pcmData, nullptr);
        
        // Demucs expects input shape [batch=1, channels=1 (or 2), time=numSamples]
        auto options = torch::TensorOptions().dtype(torch::kFloat32);
        torch::Tensor input_tensor = torch::from_blob(inData, {1, 1, numSamples}, options);
        
        // If the model expects FP16, convert the input
        if (!isFp32) {
            input_tensor = input_tensor.to(torch::kFloat16);
            module.to(torch::kFloat16);
        }

        std::vector<torch::jit::IValue> inputs;
        inputs.push_back(input_tensor);

        // Run Inference
        LOGI("Running PyTorch LibTorch forward pass...");
        at::Tensor output = module.forward(inputs).toTensor();
        
        // Convert back to FP32 if needed
        if (!isFp32) {
            output = output.to(torch::kFloat32);
        }

        // Expected Demucs output shape: [batch=1, stems=2, channels=1, time=numSamples]
        // Stem 0: Vocals, Stem 1: Backing (or vice versa, typically 0=vocals)
        float* outData = output.data_ptr<float>();

        jfloat* vocOut = env->GetFloatArrayElements(vocalsOut, nullptr);
        jfloat* bgmOut = env->GetFloatArrayElements(backingOut, nullptr);

        for (int i = 0; i < numSamples; ++i) {
            vocOut[i] = outData[0 * numSamples + i]; // Vocals
            bgmOut[i] = outData[1 * numSamples + i]; // Backing
        }

        env->ReleaseFloatArrayElements(pcmData, inData, 0);
        env->ReleaseFloatArrayElements(vocalsOut, vocOut, 0);
        env->ReleaseFloatArrayElements(backingOut, bgmOut, 0);

        LOGI("PyTorch inference complete.");

    } catch (const c10::Error& e) {
        LOGE("LibTorch Error: %s", e.what());
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exClass, (std::string("LibTorch Inference failed: ") + e.what()).c_str());
    } catch (const std::exception& e) {
        LOGE("Standard Exception: %s", e.what());
        jclass exClass = env->FindClass("java/lang/IllegalStateException");
        env->ThrowNew(exClass, "Model weights file missing or corrupted. Please redownload from Settings.");
    }
}
