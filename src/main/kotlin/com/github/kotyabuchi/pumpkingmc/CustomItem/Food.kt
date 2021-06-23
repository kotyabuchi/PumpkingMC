package com.github.kotyabuchi.pumpkingmc.CustomItem

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

abstract class Food: ConsumableItem() {

    abstract val foodLevel: Int
    abstract val exhaustion: Float

    override fun createItemStack(): ItemStack {
        val itemStack = super.createItemStack()
        itemStack.editMeta {
            val lore = it.lore() ?: mutableListOf()
            lore.add(Component.text(""))
        }
        return itemStack
    }

    override fun consume(player: Player, event: PlayerItemConsumeEvent?) {
        player.foodLevel += foodLevel
        player.exhaustion -= exhaustion
    }
}