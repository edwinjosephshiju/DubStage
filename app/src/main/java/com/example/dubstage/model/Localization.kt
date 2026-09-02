package com.example.dubstage.model

object DubStageStrings {

    fun title(lang: Language): String = when (lang) {
        Language.EN -> "DubStage"
        Language.ML -> "ഡബ്ബ് സ്റ്റേജ്"
    }

    fun tagline(lang: Language): String = when (lang) {
        Language.EN -> "Dub the scene yourself."
        Language.ML -> "രംഗങ്ങൾ സ്വന്തമായി ഡബ്ബ് ചെയ്യുക."
    }

    fun tabDubStage(lang: Language): String = when (lang) {
        Language.EN -> "DubStage"
        Language.ML -> "ഡബ്ബ് സ്റ്റേജ്"
    }

    fun tabDubForge(lang: Language): String = when (lang) {
        Language.EN -> "DubForge"
        Language.ML -> "ഡബ്ബ് ഫോർജ്"
    }

    fun tabMyDubs(lang: Language): String = when (lang) {
        Language.EN -> "My Dubs"
        Language.ML -> "എന്റെ ഡബ്ബുകൾ"
    }

    fun pickPack(lang: Language): String = when (lang) {
        Language.EN -> "Choose a dub pack"
        Language.ML -> "ഡബ്ബ് പാക്ക് തിരഞ്ഞെടുക്കുക"
    }

    fun linesCount(lang: Language, count: Int): String = when (lang) {
        Language.EN -> "$count lines"
        Language.ML -> "$count ഡയലോഗുകൾ"
    }

    fun withBacking(lang: Language): String = when (lang) {
        Language.EN -> "with Backing"
        Language.ML -> "പശ്ചാത്തല സംഗീതത്തോടെ"
    }

    fun noBacking(lang: Language): String = when (lang) {
        Language.EN -> "no Backing"
        Language.ML -> "പശ്ചാത്തല സംഗീതം ഇല്ലാതെ"
    }

    fun startDubbing(lang: Language): String = when (lang) {
        Language.EN -> "Start Dubbing"
        Language.ML -> "ഡബ്ബിംഗ് ആരംഭിക്കുക"
    }

    fun microphone(lang: Language): String = when (lang) {
        Language.EN -> "Microphone"
        Language.ML -> "മൈക്രോഫോൺ"
    }

    fun micTest(lang: Language): String = when (lang) {
        Language.EN -> "Test Mic"
        Language.ML -> "മൈക്ക് പരിശോധിക്കുക"
    }

    fun lineOf(lang: Language, current: Int, total: Int): String = when (lang) {
        Language.EN -> "Line $current / $total"
        Language.ML -> "ഡയലോഗ് $current / $total"
    }

    fun playOriginal(lang: Language): String = when (lang) {
        Language.EN -> "▶ Original"
        Language.ML -> "▶ ഒറിജിനൽ"
    }

    fun record(lang: Language): String = when (lang) {
        Language.EN -> "● Record"
        Language.ML -> "● റെക്കോർഡ്"
    }

    fun recordAgain(lang: Language): String = when (lang) {
        Language.EN -> "● Record again"
        Language.ML -> "● വീണ്ടും റെക്കോർഡ്"
    }

    fun playTake(lang: Language): String = when (lang) {
        Language.EN -> "▶ My take"
        Language.ML -> "▶ എന്റെ ശബ്ദം"
    }

    fun stop(lang: Language): String = when (lang) {
        Language.EN -> "■ Stop"
        Language.ML -> "■ നിർത്തുക"
    }

    fun leaveEmpty(lang: Language): String = when (lang) {
        Language.EN -> "Leave line empty"
        Language.ML -> "ശബ്ദം ഒഴിവാക്കുക"
    }

    fun back(lang: Language): String = when (lang) {
        Language.EN -> "‹ Back"
        Language.ML -> "‹ പിന്നോട്ട്"
    }

    fun next(lang: Language): String = when (lang) {
        Language.EN -> "Next ›"
        Language.ML -> "അടുത്തത് ›"
    }

    fun finish(lang: Language): String = when (lang) {
        Language.EN -> "Done ✓"
        Language.ML -> "പൂർത്തിയായി ✓"
    }

    fun recordingBadge(lang: Language): String = when (lang) {
        Language.EN -> "RECORDING"
        Language.ML -> "റെക്കോർഡിംഗ്"
    }

    fun speakNow(lang: Language): String = when (lang) {
        Language.EN -> "Speak now..."
        Language.ML -> "ഇപ്പോൾ സംസാരിക്കുക..."
    }

    fun hint(lang: Language): String = when (lang) {
        Language.EN -> "Listen to the original, then record. As often as you like."
        Language.ML -> "ഒറിജിനൽ കേൾക്കുക, തുടർന്ന് റെക്കോർഡ് ചെയ്യുക."
    }

    fun comparisonTitle(lang: Language): String = when (lang) {
        Language.EN -> "Waveform Comparison (Timing & Rhythm)"
        Language.ML -> "ശബ്ദ തരംഗ താരതമ്യം (ടൈമിംഗും താളവും)"
    }

    fun legendOriginal(lang: Language): String = when (lang) {
        Language.EN -> "Original Track"
        Language.ML -> "യഥാർത്ഥ ട്രാക്ക്"
    }

    fun legendTake(lang: Language): String = when (lang) {
        Language.EN -> "Your Voice Take"
        Language.ML -> "നിങ്ങളുടെ ശബ്ദം"
    }

    fun recordedNOfM(lang: Language, recorded: Int, total: Int): String = when (lang) {
        Language.EN -> "$recorded of $total lines recorded"
        Language.ML -> "$total-ൽ $recorded ഡയലോഗുകൾ റെക്കോർഡ് ചെയ്തു"
    }

    fun finaleTitle(lang: Language): String = when (lang) {
        Language.EN -> "Your Dubbed Scene Finale"
        Language.ML -> "ഡബ്ബ് ചെയ്ത രംഗം"
    }

    fun playMaster(lang: Language): String = when (lang) {
        Language.EN -> "▶ Play Full Master Scene"
        Language.ML -> "▶ പൂർണ്ണ രംഗം പ്ലേ ചെയ്യുക"
    }

    fun saveAsVideo(lang: Language): String = when (lang) {
        Language.EN -> "Save Scene Dub"
        Language.ML -> "ഡബ്ബ് ചെയ്ത രംഗം സേവ് ചെയ്യുക"
    }

    fun backToLines(lang: Language): String = when (lang) {
        Language.EN -> "‹ Back to the lines"
        Language.ML -> "‹ ഡയലോഗുകളിലേക്ക് മടങ്ങുക"
    }

    fun forgeTitle(lang: Language): String = when (lang) {
        Language.EN -> "DubForge: Scene & Pack Builder"
        Language.ML -> "ഡബ്ബ് ഫോർജ്: രംഗ & പാക്ക് നിർമ്മാണം"
    }

    fun autoDetectClips(lang: Language): String = when (lang) {
        Language.EN -> "Auto-Detect Dialogue Clips"
        Language.ML -> "ഡയലോഗ് ക്ലിപ്പുകൾ സ്വയം കണ്ടെത്തുക"
    }

    fun sensitivity(lang: Language): String = when (lang) {
        Language.EN -> "Detection Sensitivity"
        Language.ML -> "കണ്ടെത്തൽ കൃത്യത"
    }

    fun buildPack(lang: Language): String = when (lang) {
        Language.EN -> "Build Dub Pack"
        Language.ML -> "ഡബ്ബ് പാക്ക് നിർമ്മിക്കുക"
    }

    fun tabSettings(lang: Language): String = when (lang) {
        Language.EN -> "Settings"
        Language.ML -> "ക്രമീകരണങ്ങൾ"
    }

    fun settingsTitle(lang: Language): String = when (lang) {
        Language.EN -> "Settings & Studio Preferences"
        Language.ML -> "ക്രമീകരണങ്ങൾ"
    }

    fun manageDubPacks(lang: Language): String = when (lang) {
        Language.EN -> "Manage Dub Packs"
        Language.ML -> "ഡബ്ബ് പാക്കുകൾ നിയന്ത്രിക്കുക"
    }

    fun appTheme(lang: Language): String = when (lang) {
        Language.EN -> "App Theme"
        Language.ML -> "ആപ്പ് തീം"
    }

    fun storageManagement(lang: Language): String = when (lang) {
        Language.EN -> "Storage & Folders"
        Language.ML -> "സ്റ്റോറേജും ഫോൾഡറുകളും"
    }

    fun audioGpuSettings(lang: Language): String = when (lang) {
        Language.EN -> "Audio & Hardware GPU"
        Language.ML -> "ഓഡിയോ & ഹാർഡ്‌വെയർ GPU"
    }

    fun deletePack(lang: Language): String = when (lang) {
        Language.EN -> "Delete Pack"
        Language.ML -> "പാക്ക് നീക്കം ചെയ്യുക"
    }

    fun clearAllPacks(lang: Language): String = when (lang) {
        Language.EN -> "Clear All Packs"
        Language.ML -> "എല്ലാ പാക്കുകളും നീക്കം ചെയ്യുക"
    }

    fun clearCache(lang: Language): String = when (lang) {
        Language.EN -> "Clear Audio Cache"
        Language.ML -> "ഓഡിയോ കാഷെ മായ്‌ക്കുക"
    }

    fun micGood(lang: Language, db: Float): String = when (lang) {
        Language.EN -> "Level %.0f dB - Good".format(db)
        Language.ML -> "നിലവാരം %.0f dB - മികച്ചത്".format(db)
    }

    fun micTooQuiet(lang: Language, db: Float): String = when (lang) {
        Language.EN -> "Level %.0f dB - Speak louder".format(db)
        Language.ML -> "നിലവാരം %.0f dB - കുറച്ചുകൂടി ഉച്ചത്തിൽ സംസാരിക്കുക".format(db)
    }

    fun aiModelsTitle(lang: Language): String = when (lang) {
        Language.EN -> "AI NEURAL MODELS & STEM ENGINES"
        Language.ML -> "AI മോഡലുകൾ & ഡെമക്സ് എഞ്ചിൻ"
    }

    fun downloadModel(lang: Language): String = when (lang) {
        Language.EN -> "Download Model"
        Language.ML -> "മോഡൽ ഡൗൺലോഡ് ചെയ്യുക"
    }

    fun downloadingModel(lang: Language, percent: Int): String = when (lang) {
        Language.EN -> "Downloading ($percent%)..."
        Language.ML -> "ഡൗൺലോഡ് ചെയ്യുന്നു ($percent%)..."
    }

    fun modelInstalled(lang: Language): String = when (lang) {
        Language.EN -> "Installed (On-Device GPU Ready)"
        Language.ML -> "ഇൻസ്റ്റാൾ ചെയ്തു (GPU റെഡി)"
    }

    fun deleteModel(lang: Language): String = when (lang) {
        Language.EN -> "Delete Model"
        Language.ML -> "മോഡൽ നീക്കം ചെയ്യുക"
    }
}
