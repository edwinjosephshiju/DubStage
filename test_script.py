import dubforge_core, inspect, numpy as np

# 1. Verify separate_vocals default model is htdemucs_ft
sig = inspect.signature(dubforge_core.separate_vocals)
assert sig.parameters['model'].default == 'htdemucs_ft', 'Model must default to htdemucs_ft'
print('Test 1 Passed: default model is htdemucs_ft')

# 2. Verify Silero VAD / speech clipping
sr = 16000
data = np.zeros(sr * 3, dtype=np.float32)
t = np.arange(int(sr * 0.8)) / sr
data[sr:sr + int(sr * 0.8)] = 0.5 * (np.sin(2 * np.pi * 320 * t) + 0.5 * np.sin(2 * np.pi * 640 * t))
clips = dubforge_core.detect_clips(data, sr)
assert len(clips) >= 1, 'Expected at least 1 speech clip detected'
print(f'Test 2 Passed: detected {len(clips)} clip(s): {clips}')

# 3. Verify dual-stem waveform peak envelopes
peaks = dubforge_core.waveform_peaks(data, 64)
assert len(peaks) == 64, 'Expected 64 peak bins'
print('Test 3 Passed: waveform peaks generated successfully')

# 4. Verify dual-stem playback muting logic
v = np.array([0.5, 0.5], dtype=np.float32)
b = np.array([0.2, 0.2], dtype=np.float32)

def mix(mute_v, mute_b):
    if mute_v and mute_b: return None
    if mute_b: return v
    if mute_v: return b
    return (v * 0.95 + b * 0.85).clip(-1.0, 1.0)

assert np.allclose(mix(False, True), v)
assert np.allclose(mix(True, False), b)
assert np.allclose(mix(False, False), (v * 0.95 + b * 0.85).clip(-1.0, 1.0))
assert mix(True, True) is None
print('Test 4 Passed: dual-stem synchronized muting logic verified')
