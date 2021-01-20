package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class MenuButtonPlaySound(val sound: Sound): MenuButtonBase() {

    init {
        clickSound = null
        var type: Material? = null
        var name = sound.name
        if (name.startsWith("BLOCK_")) {
            name = name.removePrefix("BLOCK_")
            for (material in Material.values()) {
                if (name.startsWith(material.name)) {
                    type = material
                    break
                }
            }
        }
        if (type == null) type = Material.NOTE_BLOCK
        menuItem = ItemStackGenerator(type).setDisplayName(sound.name).setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val pitch = if (event.isRightClick) .5f else 1f
        player.playSound(player.eyeLocation, sound, 1f, pitch)
    }
}