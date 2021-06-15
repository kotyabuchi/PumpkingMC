package com.github.kotyabuchi.pumpkingmc.Enum

import net.kyori.adventure.text.format.NamedTextColor

enum class Rarity(val color: NamedTextColor, val durabilityMultiple: Double) {
    COMMON(NamedTextColor.WHITE, 1.0),
    RARE(NamedTextColor.BLUE, 1.25),
    EPIC(NamedTextColor.DARK_PURPLE, 2.0),
    ANCIENT(NamedTextColor.RED, 3.0),
    LEGENDARY(NamedTextColor.GOLD, 5.0)
}