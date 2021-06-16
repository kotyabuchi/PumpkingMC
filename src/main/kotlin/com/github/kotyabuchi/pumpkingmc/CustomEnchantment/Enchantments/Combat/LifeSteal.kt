package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.Combat

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.AttackEnchantMaster
import com.github.kotyabuchi.pumpkingmc.Enum.ToolType
import com.github.kotyabuchi.pumpkingmc.Utility.addSome
import org.bukkit.Particle
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.round

object LifeSteal: AttackEnchantMaster("LIFE_STEAL") {

    private const val maxLevel = 3
    private val itemTarget = EnchantmentTarget.WEAPON

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
        return (ToolType.AXE.includes(item) || ToolType.SWORD.includes(item) ||
                ToolType.BOW.includes(item)|| ToolType.TRIDENT.includes(item))
    }

    override fun doHitAction(target: Entity, damager: LivingEntity, level: Int, event: EntityDamageByEntityEvent) {
        damager.health + (event.finalDamage * 0.5 * level)
        damager.world.spawnParticle(Particle.VILLAGER_HAPPY, damager.location.addSome(y = damager.eyeHeight / 2), 20, .65, damager.eyeHeight / 2 + .1, .65)
    }
}