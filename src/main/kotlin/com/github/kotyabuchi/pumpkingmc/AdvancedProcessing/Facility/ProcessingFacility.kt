package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility

import com.github.kotyabuchi.pumpkingmc.Menu.MenuBase
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ProcessingFacility {

    val facilityModelItem: ItemStack
    val facilityMenu: MenuBase

    fun openFacilityMenu(player: Player) {
        player.getStatus().openMenu(facilityMenu)
    }
}