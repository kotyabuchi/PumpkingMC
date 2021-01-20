package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import org.bukkit.Sound
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

open class MenuButtonBase {

    lateinit var menuItem: ItemStack
    var clickSound: Sound? = Sound.UI_BUTTON_CLICK

    open fun clickEvent(event: InventoryClickEvent) {}
}