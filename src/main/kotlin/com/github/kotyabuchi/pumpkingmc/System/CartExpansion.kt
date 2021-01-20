package com.github.kotyabuchi.pumpkingmc.System

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.vehicle.VehicleMoveEvent

class CartExpansion: Listener {

    @EventHandler
    fun onPass(event: BlockRedstoneEvent) {
        val block = event.block
        Bukkit.broadcastMessage(block.type.name)
    }

    @EventHandler
    fun onMove(event: VehicleMoveEvent) {
        if (event.from.block == event.to.block) return
        val block = event.to.block
        if (block.type != Material.DETECTOR_RAIL) return
        
    }
}