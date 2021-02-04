package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ArcShot
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.StrongShoot
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.drawCircle
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import de.tr7zw.changeme.nbtapi.NBTEntity
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import kotlin.math.*
import kotlin.random.Random

object Archery: JobClassMaster("ARCHERY") {

    private val passBlocks: MutableSet<Material> = mutableSetOf()
    private val gravityShot: MutableList<Player> = mutableListOf()

    init {
        Material.values().forEach {
            if (!it.isSolid) passBlocks.add(it)
        }
        addTool(Material.BOW)
        addTool(Material.CROSSBOW)
        addAction(SkillCommand.RRR, 50, fun (player: Player) {
            StrongShoot.enableSkill(player, player.getStatus().getJobClassStatus(this).getLevel())
        })
        addAction(SkillCommand.LLL, 400, fun (player: Player) {
            ArcShot.enableSkill(player, player.getStatus().getJobClassStatus(this).getLevel())
        })
        addAction(SkillCommand.LRL, 200, fun (player: Player) {
            if (!gravityShot.contains(player)) {
                gravityShot.add(player)
                player.sendActionMessage("&eGravityShoot ready")
            }
        })
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val arrow = event.damager as? Arrow ?: return
        val player = arrow.shooter as? Player ?: return
        val entity = event.entity
        if (entity !is LivingEntity) return

        val nbte = NBTEntity(arrow)
        if (nbte.hasKey("Paper.Origin") && !arrow.persistentDataContainer.has(NamespacedKey(instance, "Disable_LongShotBonus"), PersistentDataType.BYTE)) {
            val doubleList = nbte.getDoubleList("Paper.Origin")
            val loc = Location(entity.world, doubleList[0], doubleList[1], doubleList[2])
            val distance = entity.location.distance(loc)
            if (distance >= 20) {
                val multiple = 1 + distance / 100.0
                event.damage *= multiple
                player.sendActionMessage("&9☆Long range bonus x${multiple.floor2Digits()}")
            }
        }
        player.getStatus().addSkillExp(this, event.finalDamage)
    }

    @EventHandler
    fun onShot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val jobClassStatus = player.getStatus().getJobClassStatus(this)
        val level = jobClassStatus.getLevel()
        val arrow = event.projectile as? Arrow ?: return
        if (gravityShot.contains(player)) shootGravityArrow(player, arrow, level)
    }

    @EventHandler
    fun onHitBlock(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return
        val block = event.hitBlock
        val entity = event.hitEntity

        if (block != null || entity != null) {
            if (arrow.persistentDataContainer.has(NamespacedKey(instance, "Gravity_Arrow"), PersistentDataType.INTEGER)) hitGravityArrow(arrow)
        }
    }

    private fun shootGravityArrow(player: Player, arrow: Arrow, level: Int) {
        gravityShot.remove(player)
        arrow.persistentDataContainer.set(NamespacedKey(instance, "Gravity_Arrow"), PersistentDataType.INTEGER, level)
    }

    private fun hitGravityArrow(arrow: Arrow) {
        val level = arrow.persistentDataContainer.getOrDefault(NamespacedKey(instance, "Gravity_Arrow"), PersistentDataType.INTEGER, 1)
        val location = arrow.location
        val stand = EntityType.ARMOR_STAND.entityClass?.let { location.world?.spawn(location, it) { stand ->
            if (stand is ArmorStand) {
                stand.isVisible = false
                stand.isMarker = true
                stand.isSmall = true
                stand.isSilent = true
                stand.setGravity(false)
                stand.setAI(false)
            }
        }} as ArmorStand
        object : BukkitRunnable() {
            var count = 0
            val distance = (level / 100.0).floor2Digits()
            override fun run() {
                if (count >= 20 * 5) {
                    cancel()
                } else {
                    stand.getNearbyEntities(distance, distance, distance).forEach {
                        val vel = it.velocity.clone().add(stand.location.clone().toVector().subtract(it.location.clone().toVector()).normalize().multiply(0.15))
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
                count++
            }
        }.runTaskTimer(instance, 0, 1)
    }
}