package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.RedstoneRail
import org.bukkit.entity.Entity
import org.bukkit.entity.Minecart
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class CartExpansion: Listener {

    private val speedKeeper = mutableMapOf<Entity, BukkitTask>()

    @EventHandler
    fun onPass(event: BlockRedstoneEvent) {
        val block = event.block
        if (block.type != Material.DETECTOR_RAIL) return
        val blockData = block.blockData as RedstoneRail
        if (blockData.isPowered) return

        val blockCenter = block.location.add(.5, .0, .5)
        val carts = blockCenter.getNearbyEntitiesByType(Minecart::class.java, 0.8)
        val downBlock = block.getRelative(BlockFace.DOWN)

        when (downBlock.type) {
            Material.GOLD_BLOCK -> {
                carts.forEach { cart ->
                    cart.maxSpeed = 1.0
                    if (!speedKeeper.containsKey(cart)) {
                        speedKeeper[cart] = object : BukkitRunnable() {
                            var count = 0
                            override fun run() {
                                try {
                                    if (cart.isDead || count > 4 * 5) {
                                        cancel()
                                        speedKeeper.remove(cart)
                                        return
                                    }
                                    count++
                                    val vel = cart.velocity
                                    if (vel.length() == 0.0) return
                                    vel.x = 0.0
                                    vel.z = 0.0
                                    vel.y = .75
                                    cart.velocity = vel
                                } catch (e: IllegalArgumentException) {
                                    Bukkit.broadcastMessage(cart.velocity.toString())
                                }
                            }
                        }.runTaskTimer(instance, 0, 5)
                    }
                }
            }
            else -> {
                
            }
        }
    }
}