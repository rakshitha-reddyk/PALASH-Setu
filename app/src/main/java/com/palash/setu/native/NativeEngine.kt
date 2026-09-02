package com.palash.setu.native

class NativeEngine {
    init {
        System.loadLibrary("palash_native")
    }

    external fun initVoskASR(modelPath: String): Boolean
    external fun translateNMT(sourceText: String, targetLanguage: String): String
    external fun synthesizePiperTTS(text: String, voicePath: String): ByteArray
}
