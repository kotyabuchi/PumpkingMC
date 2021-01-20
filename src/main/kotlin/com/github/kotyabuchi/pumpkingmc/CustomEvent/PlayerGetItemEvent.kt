package com.github.kotyabuchi.pumpkingmc.CustomEvent

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class PlayerGetItemEvent(player: Player, var item: ItemStack?): PlayerEvent(player), Cancellable {

    private var isCancelled = false
    val inventory = player.inventory

    companion object {
        private val HANDLERS = HandlerList()
        @JvmStatic private fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }
}