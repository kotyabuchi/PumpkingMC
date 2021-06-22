package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Menu.HomeMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.Home
import com.github.kotyabuchi.pumpkingmc.System.Player.PlayerStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.changeHomeIcon
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag

class ChangeHomeIconButton(private val home: Home, private val playerStatus: PlayerStatus, private val icon: Material): MenuButtonBase() {

    init {
        menuItem = ItemStackGenerator(icon).setDisplayName(icon.name.upperCamelCase()).setMenuItemTag().setFlag(ItemFlag.HIDE_ATTRIBUTES).generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        if (home.homeId == null) return
        changeHomeIcon(playerStatus.player, home, icon)
        home.changeIcon(icon)
        playerStatus.closeMenu()
        playerStatus.openMenu(HomeMenu(playerStatus.player))
    }
}