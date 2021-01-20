package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Utility.addItemOrDrop
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.round

class Telekinesis: CustomEnchantmentMaster("Telekinesis") {

    private val maxLevel = 1
    private val startLevel = 1
    private val itemTarget = EnchantmentTarget.TOOL
    private val isTreasure = false
    private val isCursed = false

    private val blocks = mutableMapOf<Block, Player>()
    private val entities = mutableMapOf<Entity, Player>()

    override fun getProbability(expCost: Int): Int {
        return round(expCost * 1.5).toInt()
    }

    override fun getMaxLevel(): Int {
        return maxLevel
    }

    override fun getStartLevel(): Int {
        return startLevel
    }

    override fun getItemTarget(): EnchantmentTarget {
        return itemTarget
    }

    override fun isTreasure(): Boolean {
        return isTreasure
    }

    override fun isCursed(): Boolean {
        return isCursed
    }

    override fun conflictsWith(other: Enchantment): Boolean {
        return false
    }

    override fun canEnchantItem(item: ItemStack): Boolean {
        return (EnchantmentTarget.TOOL.includes(item) || EnchantmentTarget.WEAPON.includes(item) ||
                EnchantmentTarget.BOW.includes(item) || EnchantmentTarget.CROSSBOW.includes(item) ||
                EnchantmentTarget.TRIDENT.includes(item))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBreak(event: BlockMineEvent) {
        val player = event.player
        if (event.isCancelled) return
        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) return
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) return
        val meta = item.itemMeta ?: return
        if (!meta.hasEnchant(this)) return
        blocks[event.block] = player
        if (event.expToDrop == 0) return
        player.giveExp(event.expToDrop, true)
        event.expToDrop = 0
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDropFromBlock(event: BlockDropItemEvent) {
        val block = event.block
        val player = blocks[block] ?: return
        val inv = player.inventory

        event.items.forEach {
            inv.addItemOrDrop(player, it.itemStack)
        }
        event.items.clear()
        blocks.remove(block)
    }

    @EventHandler
    fun onShoot(event: ProjectileLaunchEvent) {
        val entity = event.entity
        val player = entity.shooter as? Player ?: return
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return
        if (!meta.hasEnchant(this)) return
        object : BukkitRunnable() {
            override fun run() {
                entity.persistentDataContainer.set(enchantKey, PersistentDataType.BYTE, 1)
            }
        }.runTaskLater(instance, 1)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onKill(event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        var damager = event.damager
        if (damager is Player) {
            val item = damager.inventory.itemInMainHand
            val meta = item.itemMeta ?: return
            if (!meta.hasEnchant(this)) return
        } else if (damager is Projectile) {
            if (!damager.persistentDataContainer.has(enchantKey, PersistentDataType.BYTE)) return
            damager = damager.shooter as? Player ?: return
        } else {
            return
        }
        if (entity.health - event.finalDamage <= 0) entities[entity] = damager
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDropFromEntity(event: EntityDeathEvent) {
        val entity = event.entity
        val player = entities[entity] ?: return
        val inv = player.inventory

        event.drops.forEach {
            inv.addItemOrDrop(player, it)
        }
        event.drops.clear()
        player.giveExp(event.droppedExp, true)
        event.droppedExp = 0
        entities.remove(entity)
    }
}