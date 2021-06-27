package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.Combat

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.Enum.ToolType
import com.github.kotyabuchi.pumpkingmc.Utility.addSome
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Merchant
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.round

object Homing: CustomEnchantmentMaster("HOMING") {

    private const val maxLevel = 3
    private val itemTarget = EnchantmentTarget.BOW

    private val piercedKey = NamespacedKey(instance, "PierceKey")
    private val hitEntitiesMap = mutableMapOf<Projectile, MutableList<Entity>>()
    private val levelMap = mutableMapOf<Projectile, Int>()
    private val projectileMap = mutableMapOf<Projectile, BukkitTask>()

    override fun getMaxLevel(): Int {
        return maxLevel
    }

    override fun getItemTarget(): EnchantmentTarget {
        return itemTarget
    }

    override fun canEnchantItem(item: ItemStack): Boolean {
        return ToolType.BOW.includes(item)|| ToolType.TRIDENT.includes(item)
    }

    private fun containsBlackList(entity: Entity): Boolean {
        return (entity is Projectile || entity is Item || entity is ExperienceOrb || entity is Animals || entity is Hanging || entity is Merchant || entity is FallingBlock || entity is ArmorStand ||
                entity is Minecart || entity is Boat || entity is LightningStrike || entity is AreaEffectCloud)
    }

    @EventHandler
    fun onShootArrow(event: EntityShootBowEvent) {
        val bow = event.bow ?: return
        val meta = bow.itemMeta ?: return

        if (!meta.hasEnchant(this)) return
        val level = meta.getEnchantLevel(this)

        val arrow = event.projectile as? Projectile ?: return
        val shooter = event.entity
        hitEntitiesMap[arrow] = mutableListOf()
        levelMap[arrow] = level

        projectileMap[arrow] = object : BukkitRunnable() {
            override fun run() {
                if (arrow.isDead || arrow.isOnGround) {
                    removeArrow(arrow)
                } else {
                    shootNearEntity(arrow, shooter, level)
                }
            }
        }.runTaskTimer(instance, 0, 1)
    }

    @EventHandler
    fun onHitEntity(event: EntityDamageByEntityEvent) {
        val projectile = event.damager as? Projectile ?: return
        hitEntitiesMap[projectile]?.let {
            it.add(event.entity)
            hitEntitiesMap[projectile] = it
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onHit(event: ProjectileHitEvent) {
        if (event.isCancelled) return
        val arrow = event.entity as? AbstractArrow ?: return
        val pdc = arrow.persistentDataContainer
        if (event.hitBlock != null) {
            removeArrow(arrow)
        } else if (event.hitEntity != null) {
            val shooter = arrow.shooter as? LivingEntity ?: return
            pdc.set(piercedKey, PersistentDataType.INTEGER, pdc.getOrDefault(piercedKey, PersistentDataType.INTEGER, 0) + 1)
            if (pdc.getOrDefault(piercedKey, PersistentDataType.INTEGER, 0) >= arrow.pierceLevel + 2) {
                removeArrow(arrow)
            } else {
                shootNearEntity(arrow, shooter)
            }
        }
    }

    private fun shootNearEntity(arrow: Projectile, shooter: LivingEntity, level: Int = levelMap[arrow] ?: 0) {
        if (level == 0) {
            removeArrow(arrow)
            return
        }
        val hitEntities = hitEntitiesMap[arrow] ?: mutableListOf()
        val searchDistance = 3.0 * level

        var entities = when (shooter) {
            is HumanEntity -> {
                arrow.getNearbyEntities(searchDistance, searchDistance, searchDistance).filter{ it is LivingEntity && it !is HumanEntity && !containsBlackList(it) && !hitEntities.contains(it) && it.hasLineOfSight(arrow)}
            }
            is Creature -> {
                arrow.getNearbyEntities(searchDistance, searchDistance, searchDistance).filter{ it is LivingEntity && it !is Creature && !containsBlackList(it) && !hitEntities.contains(it) && it.hasLineOfSight(arrow)}
            }
            else -> return
        }
        if (entities.isEmpty()) return
        val arrowLoc = arrow.location
        entities = entities.sortedBy { arrowLoc.distance(it.location) }
        val target = entities.first()

        val targetLoc = target.location
        if (target is LivingEntity) targetLoc.addSome(y = target.eyeHeight - .1)
        val targetVec = targetLoc.toVector().subtract(arrowLoc.toVector())
        val distance = arrowLoc.distance(targetLoc)
        val vel = arrow.velocity.add(targetVec.multiply(searchDistance - distance + 1)).multiply(1.0 / (searchDistance - distance + 1)).normalize().multiply(arrow.velocity.length() - .01)
        arrow.velocity = vel
    }

    private fun removeArrow(projectile: Projectile) {
        hitEntitiesMap.remove(projectile)
        levelMap.remove(projectile)
        projectileMap[projectile]?.cancel()
        projectileMap.remove(projectile)
    }
}