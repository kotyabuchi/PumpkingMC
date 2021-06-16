package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.Utility.jump
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Spider
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.scheduler.BukkitRunnable

class EnchantedSpider: MobExpansionMaster(EntityType.SPIDER, EntityType.CAVE_SPIDER) {

    init {
        addInFightAction(0 until 10) { spider, target ->
            val spiderLoc = spider.location.toVector()
            val targetLoc = target.location.add(0.0, 0.5, 0.0).toVector()
            val webLocation = targetLoc.clone().subtract(spiderLoc).normalize().multiply(0.3)
            val web = spider.world.spawnFallingBlock(spider.location.add(webLocation), Material.COBWEB.createBlockData())
            web.velocity = targetLoc.clone().subtract(spiderLoc).multiply(0.2)
        }
        addInFightAction(0 until 20) { spider, target ->
            spider.jump(target.location)
        }
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
