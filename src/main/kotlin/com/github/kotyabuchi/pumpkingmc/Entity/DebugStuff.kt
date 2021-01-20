package com.github.kotyabuchi.pumpkingmc.Entity

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class DebugStuff: Listener {

    val hotbars = mutableMapOf<Player, MutableList<ItemStack?>>()
    val selectMode = mutableListOf<Player>()
    
    @EventHandler
    fun onSwap(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        val item = event.offHandItem ?: return
        val meta = item.itemMeta ?: return
        if (meta.displayName != ChatColor.GOLD.toString() + "DebugStuff") return
        event.isCancelled = true
        selectMode.add(player)
        val hotbar = mutableListOf<ItemStack?>()
        val inventory = player.inventory
        
        for (i in 0 until 9) {
            hotbar.add(i, inventory.getItem(i))
            inventory.setItem(i, null)
        }
        hotbars[player] = hotbar
        inventory.addItem(ItemStack(Material.VILLAGER_SPAWN_EGG))
        inventory.addItem(ItemStack(Material.ZOMBIE_SPAWN_EGG))
        inventory.addItem(ItemStack(Material.SKELETON_SPAWN_EGG))
        inventory.addItem(ItemStack(Material.CREEPER_SPAWN_EGG))
        inventory.addItem(ItemStack(Material.SPIDER_SPAWN_EGG))
        inventory.addItem(ItemStack(Material.ENDERMAN_SPAWN_EGG))
        inventory.addItem(ItemStack(Material.DROWNED_SPAWN_EGG))
    }
    
    @EventHandler
    fun onSelect(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val player = event.player
        val item = player.inventory.itemInMainHand.clone()
        val meta = item.itemMeta ?: return
        if (!event.action.name.startsWith("RIGHT_CLICK")) return
        if (selectMode.contains(player) && item.type.name.endsWith("SPAWN_EGG")) {
            event.isCancelled = true
            selectMode.remove(player)
            val lore = mutableListOf<String>()
            lore.add(ChatColor.DARK_PURPLE.toString() + "TYPE: " + item.type.name.split("_")[0])
            for (i in 0 until 9) {
                if (hotbars[player]!![i] != null && hotbars[player]!![i]!!.hasItemMeta() && hotbars[player]!![i]!!.itemMeta?.displayName == ChatColor.GOLD.toString() + "DebugStuff") {
                    val stuff = hotbars[player]!![i]!!
                    val stuffMeta = stuff.itemMeta ?: return
                    stuffMeta.lore = lore
                    stuff.itemMeta = stuffMeta
                    player.inventory.setItem(i, stuff)
                } else {
                    player.inventory.setItem(i, hotbars[player]!![i])
                }
            }
        } else if (item.hasItemMeta() && meta.displayName == ChatColor.GOLD.toString() + "DebugStuff") {
            val lore = meta.lore ?: return
            val block = player.getTargetBlockExact(50) ?: return
            val entity = player.world.spawnEntity(block.location.add(.5, 1.0, .5), EntityType.valueOf(lore[0]!!.split(" ")[1]))
            entity.persistentDataContainer.set(NamespacedKey(instance, "DebugEntity"), PersistentDataType.BYTE, 1)
        }
    }
}
