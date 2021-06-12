package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.GameMode
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.round

object ExpBoost: CustomEnchantmentMaster("EXP_BOOST") {

    private const val maxLevel = 3
    private val itemTarget = EnchantmentTarget.TOOL

    private val entities = mutableMapOf<Entity, Byte>()

    override fun getProbability(expCost: Int): Int {
        return round(expCost.toDouble() / rarity.weight).toInt()
    }

    override fun getMaxLevel(): Int {
        return maxLevel
    }

    override fun getItemTarget(): EnchantmentTarget {
        return itemTarget
    }

    override fun canEnchantItem(item: ItemStack): Boolean {
        return (EnchantmentTarget.TOOL.includes(item) || EnchantmentTarget.WEAPON.includes(item) ||
                EnchantmentTarget.BOW.includes(item) || EnchantmentTarget.CROSSBOW.includes(item) ||
                EnchantmentTarget.TRIDENT.includes(item))
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBreak(event: BlockMineEvent) {
        if (event.isCancelled) return
        val player = event.player
        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) return
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) return
        val meta = item.itemMeta ?: return
        if (!meta.hasEnchant(this)) return
        val level = meta.getEnchantLevel(this)
        if (event.expToDrop == 0) return
        event.expToDrop = round(event.expToDrop * (1 + (level * 2 / 10.0))).toInt()
    }

    @EventHandler
    fun onShoot(event: ProjectileLaunchEvent) {
        val entity = event.entity
        val player = entity.shooter as? Player ?: return
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return
        if (!meta.hasEnchant(this)) return
        val level = meta.getEnchantLevel(this)
        object : BukkitRunnable() {
            override fun run() {
                entity.persistentDataContainer.set(key, PersistentDataType.BYTE, level.toByte())
            }
        }.runTaskLater(instance, 1)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onKill(event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        val damager = event.damager
        val level: Byte
        if (damager is Player) {
            val item = damager.inventory.itemInMainHand
            val meta = item.itemMeta ?: return
            if (!meta.hasEnchant(this)) {
                entities.remove(entity)
                return
            }
            level = meta.getEnchantLevel(this).toByte()
        } else if (damager is Projectile) {
            if (!damager.persistentDataContainer.has(key, PersistentDataType.BYTE)) {
                entities.remove(entity)
                return
            }
            level = damager.persistentDataContainer.get(key, PersistentDataType.BYTE) ?: return
        } else {
            return
        }
        if (entity.health - event.finalDamage <= 0) entities[entity] = level
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onDropFromEntity(event: EntityDeathEvent) {
        val entity = event.entity
        val level = entities[entity] ?: return
        event.droppedExp = round(event.droppedExp * (1 + (level * 2 / 10.0))).toInt()
        entities.remove(entity)
    }
}