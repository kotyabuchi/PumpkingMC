package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.Utility.jump
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.entity.CaveSpider
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Spider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.scheduler.BukkitRunnable

class EnchantedSpider: Listener {
    
    @EventHandler
    fun onTarget(event: EntityTargetLivingEntityEvent) {
        val spider = event.entity as? Spider ?: return
        if (spider is CaveSpider) return
        object: BukkitRunnable() {
            override fun run() {
                val target = spider.target
                if (spider.isDead  || spider.target == null || target == null) {
                    cancel()
                } else {
                    val spiderLoc = spider.location.toVector()
                    val targetLoc = target.location.add(0.0, 0.5, 0.0).toVector()
                    val webLocation = targetLoc.clone().subtract(spiderLoc).normalize().multiply(0.3)
                    val web = spider.world.spawnFallingBlock(spider.location.add(webLocation), Material.COBWEB.createBlockData())
                    web.velocity = targetLoc.clone().subtract(spiderLoc).multiply(0.2)
                }
            }
        }.runTaskTimer(instance, 10, 20 * 3)
    
        object: BukkitRunnable() {
            override fun run() {
                val target = spider.target
                if (spider.isDead  || spider.target == null || target == null) {
                    cancel()
                } else {
                    spider.jump(target.location)
                }
            }
        }.runTaskTimer(instance, 20, 20 * 2)
    }
    
    @EventHandler
    fun onLand(event: EntityChangeBlockEvent) {
        val entity = event.entity as? FallingBlock ?: return
        val material = entity.blockData.material
        if (material == Material.COBWEB) {
            object : BukkitRunnable() {
                override fun run() {
                    if (event.block.type == Material.COBWEB) event.block.type = Material.AIR
                }
            }.runTaskLater(instance, 20 * 5)
        }
    }
    
    @EventHandler
    fun onDrop(event: EntityDropItemEvent) {
        val entity = event.entity as? FallingBlock ?: return
        val material = entity.blockData.material
        if (material == Material.COBWEB) {
            event.isCancelled = true
            entity.remove()
        }
    }
    
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val spider = event.entity as? Spider ?: return
        if (spider.target == null) return
        if (event.cause == EntityDamageEvent.DamageCause.FALL) event.isCancelled = true
    }
}
