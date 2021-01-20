package com.github.kotyabuchi.pumpkingmc.Entity.Friendly

import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class AnimalExpansion: Listener {

    @EventHandler
    fun onClick(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked as? Animals ?: return
        if (player.inventory.itemInMainHand.type.isAir && player.inventory.itemInOffHand.type.isAir) {
            if (player.passengers.isEmpty()) {
                player.addPassenger(entity)
            } else {
                var passenger = player.passengers.first()
                while (passenger.passengers.isNotEmpty()) {
                    passenger = passenger.passengers.first()
                }
                passenger.addPassenger(entity)
            }
        }
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        var passengers = mutableListOf<Entity>()
        passengers.addAll(player.passengers)
        for (i in 0 until passengers.size) {
            if (passengers[i].passengers.isEmpty()) {

            }
        }
        player.passengers.forEach {
            player.removePassenger(it)
        }
    }
}