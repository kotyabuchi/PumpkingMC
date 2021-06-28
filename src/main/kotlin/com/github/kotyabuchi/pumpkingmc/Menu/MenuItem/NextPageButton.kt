package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class NextPageButton(page: Int, allPage: Int): MenuButtonBase() {

    init {
        menuItem = ItemStackGenerator(Material.ARROW).setDisplayName("Next page $page / $allPage").setModelData(101).setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        player.getStatus().nextPage()
    }
}