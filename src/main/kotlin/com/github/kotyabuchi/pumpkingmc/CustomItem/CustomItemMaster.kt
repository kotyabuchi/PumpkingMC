package com.github.kotyabuchi.pumpkingmc.CustomItem

import com.github.kotyabuchi.pumpkingmc.instance
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

open class CustomItemMaster(val baseMaterial: Material, val itemKey: String): Listener {

    fun registerRecipe(recipe: Recipe) {
        instance.server.addRecipe(recipe)
    }

    fun hasItem(player: Player): Pair<Boolean, ItemStack?> {
        val inv = player.inventory
        for (content in inv.contents) {
            if (isCustomItem(content)) {
                return (true to content)
            }
        }
        return (false to null)
    }

    fun isCustomItem(item: ItemStack?): Boolean {
        if (item == null) {
            return false
        } else {
            if (item.type != baseMaterial) return false
            val nbti = NBTItem(item)
            return nbti.hasKey(itemKey)
        }
    }
}