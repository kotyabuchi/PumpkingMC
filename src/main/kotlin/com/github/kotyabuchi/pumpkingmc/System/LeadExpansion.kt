package com.github.kotyabuchi.pumpkingmc.System

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class LeadExpansion: Listener {

    @EventHandler
    fun onClick(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked as? LivingEntity ?: return
        val item = player.inventory.getItem(event.hand) ?: return
        if (item.type != Material.LEAD) return
        if (entity.isLeashed) return
        event.isCancelled = true
        if (entity.setLeashHolder(player)) {
            item.amount--
            player.inventory.setItem(event.hand, item)
        }
    }
}