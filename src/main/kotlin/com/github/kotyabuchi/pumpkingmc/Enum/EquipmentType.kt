package com.github.kotyabuchi.pumpkingmc.Enum

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface EquipmentType {
    val materialCost: Int?

    fun includes(item: Material): Boolean

    fun includes(item: ItemStack): Boolean = includes(item.type)

    fun getEquipmentName(): String
}