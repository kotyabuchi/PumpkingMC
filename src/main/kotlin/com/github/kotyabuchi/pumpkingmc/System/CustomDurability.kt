package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Utility.isArmors
import com.github.kotyabuchi.pumpkingmc.Utility.isTools
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemMendEvent
import org.bukkit.inventory.EquipmentSlot

class CustomDurability: Listener {

    @EventHandler
    fun onPickExpOrb(event: PlayerItemMendEvent) {
        val player = event.player
        val item = event.item
        player.inventory.contents.forEachIndexed { index, itemStack ->
            if (itemStack == item) {
                val itemExpansion = ItemExpansion(item)
                itemExpansion.increaseDurability(event.repairAmount)
                player.inventory.setItem(index, itemExpansion.item)
            }
        }
    }

    @EventHandler
    fun onDamage(event: PlayerItemDamageEvent) {
        event.isCancelled = true
        val player = event.player
        val item = ItemExpansion(event.item)
        val inv = player.inventory
        item.reduceDurability(event.damage)
        if (event.item.type.isArmors()) {
            val equipment = player.equipment ?: return
            var slot = EquipmentSlot.CHEST
            var find = false
            EquipmentSlot.values().forEach {
                if (event.item == equipment.getItem(it)) {
                    slot = it
                    find = true
                }
            }
            if (find) {
                if (item.getDurability() <= 0) {
                    equipment.setItem(slot, null)
                    player.playSound(player.eyeLocation, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1f)
                } else {
                    equipment.setItem(slot, item.item)
                }
            }
        } else if (event.item.type.isTools()) {
            if (item.getDurability() <= 0) {
                inv.setItem(inv.first(event.item), null)
                player.playSound(player.eyeLocation, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1f)
                instance.server.pluginManager.callEvent(PlayerItemBreakEvent(player, item.item))
            } else {
                inv.setItem(inv.first(event.item), item.item)
            }
        }
    }
}