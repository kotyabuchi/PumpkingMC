package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Menu.MenuBase
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class MenuButtonBackPrev(private val prevMenu: MenuBase): MenuButtonBase() {

    init {
        menuItem = ItemStackGenerator(Material.ARROW).setDisplayName("Back to ${prevMenu.title}").setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        player.getStatus().openMenu(prevMenu, prev = true)
    }
}