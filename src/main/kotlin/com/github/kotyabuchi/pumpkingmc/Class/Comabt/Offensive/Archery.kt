package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
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
    private val strongShoot: MutableList<Player> = mutableListOf()
    private val arcShotMap: MutableMap<Player, BukkitTask> = mutableMapOf()
    private val gravityShot: MutableList<Player> = mutableListOf()

    init {
        Material.values().forEach {
            if (!it.isSolid) passBlocks.add(it)
        }
        addTool(Material.BOW)
        addTool(Material.CROSSBOW)
        addAction(SkillCommand.RRR, 50, fun (player: Player) {
            if (!strongShoot.contains(player)) {
                strongShoot.add(player)
                player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
                player.sendActionMessage("&eStrongShoot ready x${1 + (player.getStatus().getJobClassStatus(this).getLevel() / 100.0).floor2Digits()}")
            }
        })
        addAction(SkillCommand.LLL, 400, fun (player: Player) {
            if (!arcShotMap.contains(player)) readyArchShot(player)
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
        val status = player.getStatus()
        val jobClassStatus = status.getJobClassStatus(this)
        val entity = event.entity
        if (entity !is LivingEntity) return

        if (arrow.persistentDataContainer.has(NamespacedKey(instance, "ArcShot_Arrow"), PersistentDataType.INTEGER)) {
            event.isCancelled = true
            entity.damage(jobClassStatus.getLevel() / 200.0, player)
            entity.noDamageTicks = 0
        } else {
            val nbte = NBTEntity(arrow)
            if (nbte.hasKey("Paper.Origin")) {
                val doubleList = nbte.getDoubleList("Paper.Origin")
                val loc = Location(entity.world, doubleList[0], doubleList[1], doubleList[2])
                val distance = entity.location.distance(loc)
                if (distance >= 20) {
                    val multiple = 1 + distance / 100.0
                    event.damage *= multiple
                    player.sendActionMessage("&9☆Long range bonus x${multiple.floor2Digits()}")
                }
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
        if (strongShoot.contains(player)) shootStrongShoot(player, arrow, level)
        if (gravityShot.contains(player)) shootGravityArrow(player, arrow, level)
        if (arcShotMap.containsKey(player)) shootArcShot(player, level)
    }

    @EventHandler
    fun onHitBlock(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return
        val block = event.hitBlock
        val entity = event.hitEntity

        if (block != null || entity != null) {
            if (arrow.persistentDataContainer.has(NamespacedKey(instance, "ArcShot_Arrow"), PersistentDataType.INTEGER)) arrow.remove()
            if (arrow.persistentDataContainer.has(NamespacedKey(instance, "Gravity_Arrow"), PersistentDataType.INTEGER)) hitGravityArrow(arrow)
        }
    }

    private fun readyArchShot(player: Player) {
        player.sendActionMessage("&eArcShoot ready")
        arcShotMap[player] = object : BukkitRunnable() {
            val level = player.getStatus().getJobClassStatus(this@Archery).getLevel()
            val circle = drawCircle((level / 100.0).floor2Digits())
            override fun run() {
                val eyeLoc = player.eyeLocation
                val block = player.getTargetBlock(passBlocks, 50)
                val distance = eyeLoc.distance(block.location.add(.5, .5, .5))
                val vec = eyeLoc.direction.normalize()
                eyeLoc.add(vec.clone().multiply(distance))
                eyeLoc.y = round(eyeLoc.y)
                circle.forEach {
                    block.world.spawnParticle(Particle.REDSTONE, eyeLoc.clone().add(it.first, .0, it.second), 1, .0, .0, .0, .0, Particle.DustOptions(Color.RED, 1f))
                }
            }
        }.runTaskTimer(instance, 0, 2)
    }

    private fun shootStrongShoot(player: Player, arrow: Arrow, level: Int) {
        strongShoot.remove(player)
        val multiple = 1 + level / 100.0
        arrow.velocity = arrow.velocity.multiply(multiple)
    }

    private fun shootGravityArrow(player: Player, arrow: Arrow, level: Int) {
        gravityShot.remove(player)
        arrow.persistentDataContainer.set(NamespacedKey(instance, "Gravity_Arrow"), PersistentDataType.INTEGER, level)
    }

    private fun shootArcShot(player: Player, level: Int) {
        arcShotMap[player]?.cancel()
        arcShotMap.remove(player)

        val eyeLoc = player.eyeLocation
        val block = player.getTargetBlock(passBlocks, 50)
        val distance = eyeLoc.distance(block.location.add(.5, .5, .5))
        val vec = eyeLoc.direction.normalize()
        val radius = (level / 100.0).pow(2.0)
        eyeLoc.add(vec.clone().multiply(distance))
        eyeLoc.y = round(eyeLoc.y)


        object : BukkitRunnable() {
            var count = 0
            override fun run() {
                if (count >= 500) {
                    cancel()
                } else {
                    for (i in 0 until 10) {
                        val r = sqrt(Random.nextDouble(0.0, radius))
                        val theta = Random.nextDouble(-Math.PI, Math.PI)
                        val x = r * cos(theta)
                        val y = Random.nextInt(50) / 10.0 + 20
                        val z = r * sin(theta)

                        val skillArrow = player.world.spawnArrow(eyeLoc.clone().add(x, y, z), Vector(.0, -1.0, .0), 1f, 0f)
                        skillArrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
                        skillArrow.shooter = player
                        skillArrow.persistentDataContainer.set(NamespacedKey(instance, "ArcShot_Arrow"), PersistentDataType.INTEGER, level)
                        count++
                    }
                }
            }
        }.runTaskTimer(instance, 0, 1)
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