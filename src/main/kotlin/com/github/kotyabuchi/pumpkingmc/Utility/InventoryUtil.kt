package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.CustomEvent.PlayerGetItemEvent
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import kotlin.math.min

fun PlayerInventory.addItemOrDrop(player: Player, vararg items: ItemStack): Boolean {
    val addedItems = mutableMapOf<Int, ItemStack>()
    val contents = this.contents

    items.forEach { item ->
        var amount = item.amount

        for ((index, itemStack) in contents.withIndex()) {
            if (itemStack == null) {
                this.setItem(index, item)
                addedItems[index] = item
                amount = 0
            } else {
                val addItemClone = item.clone()
                val checkItemClone = itemStack.clone()
                addItemClone.amount = 1
                checkItemClone.amount = 1
                if (addItemClone == checkItemClone) {
                    val canAddAmount = min(amount, itemStack.maxStackSize - itemStack.amount)
                    itemStack.amount += canAddAmount
                    addItemClone.amount = canAddAmount
                    addedItems[index] = addItemClone
                    amount -= canAddAmount
                }
            }
            if (amount <= 0) break
        }

        if (amount > 0) {
            item.amount = amount
            player.world.dropItem(player.location, item)
        }
    }

    for ((index, addedItem) in addedItems) {
        val getItemEvent = PlayerGetItemEvent(player, addedItem)
        instance.server.pluginManager.callEvent(getItemEvent)
        val itemBackup = getItemEvent.item
        if (getItemEvent.isCancelled) {
            val addedItemContent = contents[index]
            if (addedItemContent.amount == addedItem.amount) {
                this.setItem(index, null)
            } else {
                contents[index].amount -= addedItem.amount
            }
        } else if (itemBackup == null) {
            this.setItem(index, null)
        } else if (itemBackup.amount != addedItem.amount) {
            contents[index].amount -= addedItem.amount
        }
    }
    return addedItems.isNotEmpty()
}

fun Inventory.findFirst(searchItem: ItemStack): Pair<Int, ItemStack>? {
    for ((index, item) in this.storageContents.withIndex()) {
        if (item != null && !item.type.isAir) {
            if (searchItem.isSimilar(item)) return (index to item)
        }
    }
    return null
}

fun Inventory.findLast(searchItem: ItemStack): Pair<Int, ItemStack>? {
    for ((index, item) in this.storageContents.reversed().withIndex()) {
        if (item != null && !item.type.isAir) {
            if (searchItem.isSimilar(item)) return (index to item)
        }
    }
    return null
}

fun Inventory.getFirstItem(): Pair<Int, ItemStack>? {
    this.contents.forEachIndexed { index, itemStack ->
        if (itemStack != null && !itemStack.type.isAir) return (index to itemStack)
    }
    return null
}