package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity.CursedEye
import com.github.kotyabuchi.pumpkingmc.Utility.addSome
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.*
import org.bukkit.block.Container
import org.bukkit.entity.Enderman
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

class EnchantedEnderman: MobExpansionMaster(EntityType.ENDERMAN) {

    private val stateMap = mutableMapOf<Mob, State>()

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

        addStartFightAction { enderman, _ ->
            if (Random.nextBoolean()) {
                repeat(Random.nextInt(4) + 1) {
                    CursedEye.spawn(enderman as Enderman)
                }
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

        addInFightAction(0 until 5) { enderman, target ->
            val targetLoc = target.location.clone()
            val loc = targetLoc.clone().subtract(targetLoc.direction.setY(0).multiply(2))
            loc.y = targetLoc.y
            val headBlock = loc.clone().addSome(y = enderman.eyeHeight).block
            if (headBlock.type.isAir) {
                enderman.teleport(loc)
            } else {
                target.teleport(enderman.location)
                target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 1, true, false))
                if (targetLoc.clone().addSome(y = enderman.eyeHeight).block.type.isAir) {
                    enderman.teleport(targetLoc)
                }
            }
            enderman.world.playSound(enderman.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
        }

        addGetDamageAction { enderman, target ->
            if (stateMap[enderman] == State.BLACK_HOLE) return@addGetDamageAction
            enderman as Enderman
            val endermanLoc = enderman.location
            val targetLocBlock = target.location.addSome(y = enderman.eyeHeight).block
            if (!targetLocBlock.type.isAir) {
                if ((targetLocBlock.type.isInteractable || targetLocBlock.state is Container) && (0 until 100).contains(Random.nextInt(100))) {
                    doBlackHole(enderman)
                } else {
                    enderman.carriedBlock?.let { blockData ->
                        endermanLoc.world.dropItem(endermanLoc, ItemStack(blockData.material))
                    }
                    enderman.carriedBlock = targetLocBlock.blockData
                    targetLocBlock.type = Material.AIR
                }
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

    private fun doBlackHole(enderman: Enderman) {
        stateMap[enderman] = State.BLACK_HOLE
        enderman.world.playSound(enderman.location, Sound.ENTITY_ENDERMAN_STARE, 1f, 1.5f)
        enderman.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20 * 8, 3, true, true))
        val location = enderman.location.addSome(y = 1.0)
        val distance = 4.0
        val borderParticleCount = round(distance * distance * 2).toInt()
        val locationVec = location.clone().toVector()
        object : BukkitRunnable() {
            var count = 20 * 5
            override fun run() {
                if (count <= 0) {
                    stateMap[enderman] = State.FIGHTING
                    cancel()
                } else {
                    if (count >= 20) location.world.spawnParticle(Particle.PORTAL, location, 50)

                    repeat(borderParticleCount) {
                        val theta = Random.nextDouble(-Math.PI, Math.PI)
                        val p = Random.nextDouble(0.0, 1.0)
                        val phi = asin((2 * p) - 1)
                        val x = distance * cos(phi) * cos(theta)
                        val y = distance * cos(phi) * sin(theta)
                        val z = distance * sin(phi)
                        location.world.spawnParticle(
                            Particle.REDSTONE, location.clone().add(x, y, z), 1, Particle.DustOptions(
                                Color.PURPLE, 1f))
                    }

                    location.getNearbyEntities(distance, distance, distance).forEach {
                        if (it !is Enderman) {
                            val vel = it.velocity.clone().add(locationVec.clone().subtract(it.location.clone().toVector()).normalize().multiply(0.15))
                            if (vel.x > 4.0) {
                                vel.x = 4.0
                            } else if (vel.x < -4.0) {
                                vel.x = -4.0
                            }
                            if (vel.y > 4.0) {
                                vel.y = 4.0
                            } else if (vel.y < -4.0) {
                                vel.y = -4.0
                            }
                            if (vel.z > 4.0) {
                                vel.z = 4.0
                            } else if (vel.z < -4.0) {
                                vel.z = -4.0
                            }
                            it.velocity = vel
                        }
                    }
                }
                count--
            }
        }.runTaskTimer(instance, 20 * 3, 1)
    }

    enum class State {
        IDLE,
        FIGHTING,
        BLACK_HOLE
    }
}
