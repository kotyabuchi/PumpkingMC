package com.github.kotyabuchi.pumpkingmc.Enum

import org.bukkit.Material

enum class StoneEnum(val material: Material, val point: Int) {
    STONE(Material.STONE, 1),
    GRANITE(Material.GRANITE, 1),
    DIORITE(Material.DIORITE, 1),
    ANDESITE(Material.ANDESITE, 1),
    COAL_ORE(Material.COAL_ORE, 2),
    IRON_ORE(Material.IRON_ORE, 3),
    REDSTONE_ORE(Material.REDSTONE_ORE, 4),
    GOLD_ORE(Material.GOLD_ORE, 5),
    LAPIS_ORE(Material.LAPIS_ORE, 7),
    DIAMOND_ORE(Material.DIAMOND_ORE, 8),
    EMERALD_ORE(Material.EMERALD_ORE, 10);
}
