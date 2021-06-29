package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Menu.AlchemyCauldronMenu
import com.github.kotyabuchi.pumpkingmc.Menu.MenuBase
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object StoneMill: ProcessingFacility {

    private val modelItem = ItemStackGenerator(Material.GOLDEN_HOE).setModelData(102).generate()
    override val facilityModelItemMap: Map<EquipmentSlot, ItemStack> = mapOf(EquipmentSlot.HEAD to modelItem)
    override val facilityMenu: MenuBase = AlchemyCauldronMenu()
}