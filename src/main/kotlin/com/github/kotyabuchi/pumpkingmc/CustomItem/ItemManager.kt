package com.github.kotyabuchi.pumpkingmc.CustomItem

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemManager: Listener {

    val itemTypeKey: NamespacedKey = NamespacedKey(instance, "ItemType")
    private val customItems = mutableMapOf<String, ItemMaster>()

    init {
        CustomItem.values().forEach {
            registerItem(it.itemClass.itemType, it.itemClass)
        }
    }

    private fun registerItem(itemType: String, item: ItemMaster) {
        customItems[itemType] = item
    }

    fun getItem(itemType: String): ItemMaster? {
        return customItems[itemType]
    }

    fun getItem(itemStack: ItemStack): ItemMaster? {
        return getItemType(itemStack)?.let { return getItem(it) }
    }

    fun getItemType(itemStack: ItemStack): String? {
        val meta = itemStack.itemMeta ?: return null
        return meta.persistentDataContainer.get(itemTypeKey, PersistentDataType.STRING)
    }

    @EventHandler
    fun onConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val itemStack = event.item
        val item = getItem(itemStack) as? ConsumableItem ?: return
        item.consume(player, event)
    }
}