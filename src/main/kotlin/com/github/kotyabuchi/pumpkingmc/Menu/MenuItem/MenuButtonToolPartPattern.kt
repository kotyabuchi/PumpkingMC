package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Menu.SmithingMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.beginWithUpperCase
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag

class MenuButtonToolPartPattern(private val type: ToolPartType): MenuButtonBase() {

    init {
        menuItem = ItemStackGenerator(type.icon).setDisplayName(type.name.beginWithUpperCase()).setFlag(ItemFlag.HIDE_ATTRIBUTES).setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        player.getStatus().openMenu(SmithingMenu(type))
    }
}