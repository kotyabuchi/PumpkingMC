package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToolLinkedSkill
import com.github.kotyabuchi.pumpkingmc.Utility.drawCircle
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.*
import kotlin.random.Random

object ArcShot: ToolLinkedSkill {
    override val skillName: String = "ArcShot"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 200
    override val description: String = ""
    override val hasActiveTime: Boolean = true
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val lastUseTime: MutableMap<UUID, Long> = mutableMapOf()
    override val skillItemBackup: MutableMap<UUID, ItemStack> = mutableMapOf()
    override fun calcActiveTime(level: Int): Int = 40 + level / 25 * 30

    private val arcShotArrowKey: NamespacedKey = NamespacedKey(instance, "ArcShot_Arrow")

    private val passBlocks: MutableSet<Material> = mutableSetOf()
    private val arcShotMap: MutableMap<UUID, BukkitTask> = mutableMapOf()

    init {
        Material.values().forEach {
            if (!it.isSolid) passBlocks.add(it)
        }
    }

    override fun enableAction(player: Player, level: Int) {
        arcShotMap[player.uniqueId] = object : BukkitRunnable() {
            val circle = drawCircle((level / 100.0).floor2Digits())
            override fun run() {
                val eyeLoc = player.eyeLocation
                val block = player.getTargetBlock(passBlocks, 50)
                val distance = eyeLoc.distance(block.location.add(.5, .5, .5))
                val vec = eyeLoc.direction.normalize()
                eyeLoc.add(vec.clone().multiply(distance))
                eyeLoc.y = round(eyeLoc.y)
                circle.forEach {
                    block.world.spawnParticle(
                        Particle.REDSTONE, eyeLoc.clone().add(it.first, .0, it.second), 1, .0, .0, .0, .0, Particle.DustOptions(
                            Color.RED, 1f))
                }
            }
        }.runTaskTimer(instance, 0, 2)
        player.sendActionMessage("&eArcShoot ready")
    }

    override fun disableAction(player: Player) {
        arcShotMap[player.uniqueId]?.cancel()
        arcShotMap.remove(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onShot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val arrow = event.projectile as? Arrow ?: return
        if (!isEnabledSkill(player)) return
        getSkillLevel(player)?.let {
            shootArcShot(player, arrow, it, event)
            event.projectile.remove()
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val arrow = event.damager as? Arrow ?: return
        val player = arrow.shooter as? Player ?: return
        val entity = event.entity
        if (entity !is LivingEntity) return
        val pdc = arrow.persistentDataContainer

        if (pdc.has(arcShotArrowKey, PersistentDataType.INTEGER)) {
            event.isCancelled = true
            entity.damage((pdc.getOrDefault(arcShotArrowKey, PersistentDataType.INTEGER, 1)) / 200.0, player)
            entity.noDamageTicks = 0
        }
    }

    @EventHandler
    fun onHitBlock(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return
        val block = event.hitBlock

        if (block != null && arrow.persistentDataContainer.has(arcShotArrowKey, PersistentDataType.INTEGER)) {
            arrow.remove()
        }
    }

    private fun shootArcShot(player: Player, arrow: Arrow, level: Int, event: EntityShootBowEvent) {
        disableSkill(player)

        player.world.playSound(player.eyeLocation, Sound.ENTITY_ARROW_SHOOT, 1f, .7f)
        val eyeLoc = player.eyeLocation
        val block = player.getTargetBlock(passBlocks, 50)
        val distance = eyeLoc.distance(block.location.add(.5, .5, .5))
        val vec = eyeLoc.direction.normalize()
        val radius = (level / 100.0).pow(2.0)
        eyeLoc.add(vec.clone().multiply(distance))
        eyeLoc.y = round(eyeLoc.y)

        val damage = level / 200.0
        val arrowAmount = level * 1.5
        val arrowPerOneTime = round(level / 100.0 * 2).toInt()

        val pierceLevel = arrow.pierceLevel
        val enchantments = arrow.persistentDataContainer.get(NamespacedKey(instance, "Enchantments"), PersistentDataType.STRING) ?: ""

        object : BukkitRunnable() {
            var count = 0
            override fun run() {
                if (count >= arrowAmount) {
                    cancel()
                } else {
                    repeat(arrowPerOneTime) {
                        val r = sqrt(Random.nextDouble(.0, radius))
                        val theta = Random.nextDouble(-Math.PI, Math.PI)
                        val x = r * cos(theta)
                        val y = Random.nextInt(50) / 10.0 + 20
                        val z = r * sin(theta)
                        val arrowLoc = eyeLoc.clone().add(x, y, z)

                        if (it == 0) player.world.playSound(arrowLoc, Sound.ENTITY_ARROW_SHOOT, 1.5f, 1.3f)
                        val skillArrow = player.world.spawnArrow(eyeLoc.clone().add(x, y, z), Vector(.0, -1.0, .0), 1f, 0f)
                        skillArrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
                        skillArrow.shooter = player
                        skillArrow.damage = damage
                        skillArrow.pierceLevel = pierceLevel
                        skillArrow.persistentDataContainer.set(arcShotArrowKey, PersistentDataType.INTEGER, level)
                        skillArrow.persistentDataContainer.set(NamespacedKey(instance, "Disable_LongShotBonus"), PersistentDataType.BYTE, 1)
                        skillArrow.persistentDataContainer.set(NamespacedKey(instance, "Enchantments"), PersistentDataType.STRING, enchantments)
                        instance.callEvent(EntityShootBowEvent(player, event.bow, null, skillArrow, event.hand, event.force, false))
                        count++
                    }
                }
            }
        }.runTaskTimer(instance, 0, 1)
    }
}