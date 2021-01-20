package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class MenuController: Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val playerStatus = player.getStatus()
        val clickedInventory = event.clickedInventory ?: return
        val openingMenu = playerStatus.getOpeningMenu() ?: return
        val openingPage = playerStatus.getOpeningPage()
        if (clickedInventory == openingMenu.getInventory(openingPage)) {
            if (openingMenu.hasButton(event.rawSlot, openingPage)) {
                event.isCancelled = true
                openingMenu.doButtonClickEvent(event.rawSlot, event, openingPage)
            }
        }
        openingMenu.doItemClickEvent(event.rawSlot, event, openingPage)
    }

    @EventHandler
    fun onCloseMenu(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val status = player.getStatus()
        if (status.openMenuWithCloseMenu) {
            status.openMenuWithCloseMenu = false
        } else {
            status.closeMenu()
        }
    }
}