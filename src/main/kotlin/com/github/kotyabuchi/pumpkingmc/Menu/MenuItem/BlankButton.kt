package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import org.bukkit.Material

class BlankButton(material: Material = Material.BLACK_STAINED_GLASS_PANE): MenuButtonBase() {

    init {
        menuItem = ItemStackGenerator(material).setDisplayName("").setMenuItemTag().generate()
        clickSound = null
    }
}