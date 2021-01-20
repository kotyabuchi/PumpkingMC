package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

class ProjectileReflection: CustomEnchantmentMaster("Projectile Reflection") {

    private val maxLevel = 5
    private val startLevel = 1
    private val itemTarget = EnchantmentTarget.ARMOR
    private val isTreasure = false
    private val isCursed = false

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
        return EnchantmentTarget.ARMOR.includes(item)
    }

    @EventHandler
    fun onHit(event: ProjectileCollideEvent) {
        val projectile = event.entity
        val entity = event.collidedWith as? LivingEntity ?: return
        var level = 0
        entity.equipment?.armorContents?.forEach { item ->
            item?.itemMeta?.let { meta ->
                if (meta.hasEnchant(this)) {
                    level += meta.getEnchantLevel(this)
                }
            }
        }
        val probability = min(75, level * 10)
        if (Random.nextInt(100) >= probability) return

        event.isCancelled = true
        projectile.setBounce(true)

        if (entity is Player) {
            entity.playSound(projectile.location, Sound.BLOCK_ANVIL_PLACE, 0.05f, 1.5f)
        }
    }
}