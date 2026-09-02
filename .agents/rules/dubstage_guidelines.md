# DubStage Development Guidelines

- **Dual-Platform Architecture**: Always maintain and verify audio processing logic across both Python desktop scripts (`dubforge_core.py`, `DubForge.pyw`) and Android Kotlin modules (`DemucsEngine.kt`, `DubMixer.kt`, `DubStageViewModel.kt`).
- **Stem Isolation Standard**: Default separation model is `htdemucs_ft` producing two stems: `vocals` (dialogue) and `no_vocals` (BGM containing music, SFX, and foley).
- **VAD Standard**: Speech segment detection uses Silero VAD (with RMS fallback) with 16kHz audio input, 512-sample frames, hysteresis thresholds, boundary padding, and micro-pause merging.
- **Reference Repo Hygiene**: Reference directories (such as `xmrius_reference/`) must always be excluded via `.gitignore`.
- **Automated Test Script**: When `test_script.py` is present in the workspace root, GitHub Actions (`build.yml`) and local verification must run `python test_script.py` directly.
