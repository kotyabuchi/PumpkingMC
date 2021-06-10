package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Utility.hasDurability
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ItemExpansionManager: Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onCraft(event: PrepareItemCraftEvent) {
        val inv = event.inventory
        inv.result?.let {
            if (!it.type.isBlock) {
                var result = ItemExpansion(it).item
                val type = it.type
                if (type.hasDurability()) {
                    val nbti = NBTItem(result)
                    nbti.setString("UUID", UUID.randomUUID().toString())
                    result = nbti.item
                }
                inv.result = result
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onOpen(event: InventoryOpenEvent) {
        val inv = event.inventory
        if (inv.type == InventoryType.CHEST ||
                inv.type == InventoryType.BARREL ||
                inv.type == InventoryType.DISPENSER ||
                inv.type == InventoryType.HOPPER ||
                inv.type == InventoryType.DROPPER ||
                inv.type == InventoryType.MERCHANT) {
            inv.contents.forEachIndexed { index, itemStack ->
                if (itemStack != null && !itemStack.type.isBlock) {
                    val nbti = NBTItem(itemStack)
                    if (!nbti.hasKey("IS_MENU_ITEM")) inv.setItem(index, ItemExpansion(itemStack).item)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDrop(event: PlayerDropItemEvent) {
        val item = event.itemDrop
        if (item.itemStack.type.isBlock) return
        item.itemStack = ItemExpansion(item.itemStack).item
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityDrop(event: EntityDropItemEvent) {
        val item = event.itemDrop
        if (item.itemStack.type.isBlock) return
        item.itemStack = ItemExpansion(item.itemStack).item
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockDrop(event: BlockDropItemEvent) {
        val items = event.items
        for (item in items) {
            if (item.itemStack.type.isBlock) continue
            item.itemStack = ItemExpansion(item.itemStack).item
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onDeathEntity(event: EntityDeathEvent) {
        val items = event.drops
        val result = mutableListOf<ItemStack>()
        for (item in items) {
            if (item.type.isBlock) continue
            result.add(ItemExpansion(item).item)
        }
        items.clear()
        items.addAll(result)
    }
}