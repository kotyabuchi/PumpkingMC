package com.github.kotyabuchi.pumpkingmc.Enum

import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase

enum class ItemType {
    TOOL,
    WEAPON,
    ARMOR,
    AMULET,
    TOOL_PART,
    PLANT,
    FOOD,
    MATERIAL;

    fun getRegularName(): String {
        return name.upperCamelCase()
    }
}