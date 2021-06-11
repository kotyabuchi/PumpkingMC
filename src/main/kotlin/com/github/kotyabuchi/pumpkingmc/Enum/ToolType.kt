package com.github.kotyabuchi.pumpkingmc.Enum

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class ToolType {
    PICKAXE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_PICKAXE || 
                    item == Material.STONE_PICKAXE ||
                    item == Material.IRON_PICKAXE ||
                    item == Material.GOLDEN_PICKAXE ||
                    item == Material.DIAMOND_PICKAXE ||
                    item == Material.NETHERITE_PICKAXE
        }},
    AXE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_AXE ||
                    item == Material.STONE_AXE ||
                    item == Material.IRON_AXE ||
                    item == Material.GOLDEN_AXE ||
                    item == Material.DIAMOND_AXE ||
                    item == Material.NETHERITE_AXE
        }},
    SHOVEL {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_SHOVEL ||
                    item == Material.STONE_SHOVEL ||
                    item == Material.IRON_SHOVEL ||
                    item == Material.GOLDEN_SHOVEL ||
                    item == Material.DIAMOND_SHOVEL ||
                    item == Material.NETHERITE_SHOVEL
        }},
    HOE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_HOE ||
                    item == Material.STONE_HOE ||
                    item == Material.IRON_HOE ||
                    item == Material.GOLDEN_HOE ||
                    item == Material.DIAMOND_HOE ||
                    item == Material.NETHERITE_HOE
        }},
    SWORD {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_SWORD ||
                    item == Material.STONE_SWORD ||
                    item == Material.IRON_SWORD ||
                    item == Material.GOLDEN_SWORD ||
                    item == Material.DIAMOND_SWORD ||
                    item == Material.NETHERITE_SWORD
        }},
    BATTLEAXE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_AXE ||
                    item == Material.STONE_AXE ||
                    item == Material.IRON_AXE ||
                    item == Material.GOLDEN_AXE ||
                    item == Material.DIAMOND_AXE ||
                    item == Material.NETHERITE_AXE
        }},
    BOW {
        override fun includes(item: Material): Boolean {
            return item == Material.BOW ||
                    item == Material.CROSSBOW
        }},
    TRIDENT {
        override fun includes(item: Material): Boolean {
            return item == Material.TRIDENT
        }};
    
    abstract fun includes(item: Material): Boolean
    
    fun includes(item: ItemStack): Boolean = includes(item.type)
}