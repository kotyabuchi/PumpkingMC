package com.github.kotyabuchi.pumpkingmc.CustomItem

import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class ItemMaster {

    abstract val itemName: TextComponent
    val itemType: String by lazy { itemName.content() }
    abstract val modelData: Int
    abstract val material: Material
    val itemStack: ItemStack by lazy { createItemStack() }

    fun getItem(): ItemStack {
        return itemStack.clone()
    }

    open fun createItemStack(): ItemStack {
        return ItemStack(material).apply {
            this.editMeta {
                it.setCustomModelData(modelData)
                it.displayName(itemName)
                it.persistentDataContainer.set(ItemManager.itemTypeKey, PersistentDataType.STRING, itemType)
            }
        }
    }
}