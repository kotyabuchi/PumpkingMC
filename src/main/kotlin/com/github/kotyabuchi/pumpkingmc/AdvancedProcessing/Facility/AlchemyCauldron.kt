package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Menu.AlchemyCauldronMenu
import com.github.kotyabuchi.pumpkingmc.Menu.MenuBase
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object AlchemyCauldron: ProcessingFacility {

    override val facilityModelItemMap: Map<EquipmentSlot, ItemStack> = mapOf(EquipmentSlot.HEAD to ItemStack(Material.GOLDEN_HOE))
    override val facilityMenu: MenuBase = AlchemyCauldronMenu()
}