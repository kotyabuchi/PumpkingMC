package com.github.kotyabuchi.pumpkingmc.CustomEvent

import com.github.kotyabuchi.pumpkingmc.Utility.miningWithEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

object CustomEventCaller: Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        if (event.isCancelled) return
        val player = event.player
        val block = event.block
        val itemStack = player.inventory.itemInMainHand

        event.isCancelled = true

        block.miningWithEvent(player, itemStack)
    }
}