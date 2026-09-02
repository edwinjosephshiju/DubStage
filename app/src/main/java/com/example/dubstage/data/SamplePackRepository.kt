package com.example.dubstage.data

import com.example.dubstage.model.DubPack

object SamplePackRepository {

    fun getDefaultPacks(): List<DubPack> {
        // Real empty initial pack state. Packs are created in DubForge from imported video or imported folders.
        return emptyList()
    }
}
