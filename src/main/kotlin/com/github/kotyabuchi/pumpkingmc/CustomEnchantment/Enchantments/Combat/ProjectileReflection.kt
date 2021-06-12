package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.Combat

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import org.bukkit.Sound
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

object ProjectileReflection: CustomEnchantmentMaster("PROJECTILE_REFLECTION") {

    private const val maxLevel = 5
    private val itemTarget = EnchantmentTarget.ARMOR

    override fun getProbability(expCost: Int): Int {
        return round(expCost.toDouble() / rarity.weight).toInt()
    }

    override fun getMaxLevel(): Int {
        return maxLevel
    }

    override fun getItemTarget(): EnchantmentTarget {
        return itemTarget
    }

    @EventHandler
    fun onHit(event: ProjectileCollideEvent) {
        val projectile = event.entity
        val entity = event.collidedWith as? LivingEntity ?: return
        var level = 0
        entity.equipment?.armorContents?.forEach { item ->
            item.itemMeta?.let { meta ->
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