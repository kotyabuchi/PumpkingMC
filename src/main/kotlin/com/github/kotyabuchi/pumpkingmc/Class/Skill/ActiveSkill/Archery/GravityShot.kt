package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.*
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

object GravityShot: ActiveSkillMaster {
    override val skillName: String = "GravityShot"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 100
    override var description: String = ""
    override val hasActiveTime: Boolean = false
    override val activePlayerLevelMap: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val coolTimePlayers: MutableList<UUID> = mutableListOf()
    override fun calcActiveTime(level: Int): Int = 0

    private val gravityShotArrowKey: NamespacedKey = NamespacedKey(instance, "Gravity_Arrow")

    override fun enableAction(player: Player, level: Int) {
        player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
        player.sendActionMessage("&eGravityShoot ready")
    }

    override fun disableAction(player: Player) {
    }

    @EventHandler
    fun onShot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val uuid = player.uniqueId
        val arrow = event.projectile as? Arrow ?: return
        if (!isEnabledSkill(player)) return
        val level = activePlayerLevelMap[uuid] ?: 1
        arrow.persistentDataContainer.set(gravityShotArrowKey, PersistentDataType.INTEGER, level)
        disableSkill(player)
    }

    @EventHandler
    fun onHitBlock(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return
        val block = event.hitBlock
        val entity = event.hitEntity

        if ((block != null || entity != null) && arrow.persistentDataContainer.has(gravityShotArrowKey, PersistentDataType.INTEGER)) {
            hitGravityArrow(arrow)
        }
    }

    private fun hitGravityArrow(arrow: Arrow) {
        val level = arrow.persistentDataContainer.getOrDefault(gravityShotArrowKey, PersistentDataType.INTEGER, 1)
        val location = arrow.location.clone()

        object : BukkitRunnable() {
            var count = 20 * 5
            val distance = 2 + (level / 100.0).floor2Digits()
            override fun run() {
                if (count <= 0) {
                    cancel()
                } else {
                    if (count >= 20) location.world.spawnParticle(Particle.PORTAL, location, 50)

                    repeat(round(distance * distance * 2).toInt()) {
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
                        val vel = it.velocity.clone().add(location.clone().toVector().subtract(it.location.clone().toVector()).normalize().multiply(0.15))
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
                count--
            }
        }.runTaskTimer(instance, 0, 1)
    }
}