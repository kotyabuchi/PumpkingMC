package com.github.kotyabuchi.pumpkingmc.Enum

enum class Rarity(val text: String, val durabilityMultiple: Double) {
    COMMON("&fCOMMON", 1.0),
    RARE("&9RARE", 1.25),
    EPIC("&5EPIC", 2.0),
    ANCIENT("&cANCIENT", 3.0),
    LEGENDARY("&6LEGENDARY", 5.0)
}