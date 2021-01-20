package com.github.kotyabuchi.pumpkingmc.System

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.entity.EntityExplodeEvent

class WorldGuard: Listener {

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        event.blockList().clear()
    }
    
    @EventHandler
    fun onIgnite(event: BlockBurnEvent) {
        event.isCancelled = true
    }
    
    @EventHandler
    fun onSpread(event: BlockSpreadEvent) {
        if (event.source.type == Material.FIRE) event.isCancelled = true
    }
}
