package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Menu.ToolStationMenu
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import org.bukkit.Material

class CombineToolButton(private val menu: ToolStationMenu): MenuButtonBase() {

    init {
        clickSound = null
        val lore = mutableListOf("&6Left Click: Craft item")
        menuItem = ItemStackGenerator(Material.ANVIL).setDisplayName("Combine Tool").setMenuItemTag().setLore(lore).generate()
    }
}