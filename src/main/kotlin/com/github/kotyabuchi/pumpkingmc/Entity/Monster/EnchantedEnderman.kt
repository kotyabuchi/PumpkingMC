package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity.CursedEye
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Enderman
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class EnchantedEnderman: MobExpansionMaster(EntityType.ENDERMAN) {

    init {
        object : BukkitRunnable() {
            override fun run() {
                instance.server.onlinePlayers.forEach {
                    if (it.gameMode == GameMode.SURVIVAL || it.gameMode == GameMode.ADVENTURE) {
                        it.getNearbyEntities(16.0, 8.0, 16.0).forEach { entity ->
                            if (entity is Enderman && entity.target == null) {
                                entity.target = it
                                instance.server.pluginManager.callEvent(EntityTargetLivingEntityEvent(entity, it, EntityTargetEvent.TargetReason.CLOSEST_PLAYER))
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(instance, 0, 40)

        addStartFightAction { enderman ->
            if (Random.nextBoolean()) {
                CursedEye.spawn(enderman as Enderman)
            }
            object : BukkitRunnable() {
                override fun run() {
                    enderman.target?.let { target ->
                        val loc = target.location.subtract(target.location.direction.setY(0).multiply(2))
                        loc.y = target.location.y
                        enderman.teleport(loc)
                        enderman.world.playSound(enderman.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                    }
                }
            }.runTaskLater(instance, 10)
        }

        addInFightAction(0 until 5) { enderman ->
            enderman.target?.let { target ->
                val loc = target.location.subtract(target.location.direction.setY(0).multiply(2))
                loc.y = target.location.y
                enderman.teleport(loc)
                enderman.world.playSound(enderman.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
            }
        }
    }

    @EventHandler
    fun onDamagedCursedEye(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity.customName == "Cursed Eye") {
            entity.world.playSound(entity.location, Sound.BLOCK_END_PORTAL_FRAME_FILL, .5f, .5f)
        }
    }

    @EventHandler
    fun onPopBlock(event: EntityChangeBlockEvent) {
        val entity = event.entity
        if (entity is Enderman) event.isCancelled = true
    }
}
