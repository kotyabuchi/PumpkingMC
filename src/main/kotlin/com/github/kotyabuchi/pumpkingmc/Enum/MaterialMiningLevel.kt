package com.github.kotyabuchi.pumpkingmc.Enum

import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase

enum class MaterialMiningLevel {
    WOOD,
    STONE,
    IRON,
    GOLDEN,
    DIAMOND,
    NETHERITE;

    fun getRegularName(): String {
        return name.upperCamelCase()
    }
}