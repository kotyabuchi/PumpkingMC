package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.Utility.addEnchantment
import com.github.kotyabuchi.pumpkingmc.Utility.getEnchantLevel
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent

abstract class AttackEnchantMaster(name: String): CustomEnchantmentMaster(name) {

    abstract fun doHitAction(target: Entity, damager: LivingEntity, level: Int, event: EntityDamageByEntityEvent)

    @EventHandler
    fun onDamagedEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val damage = event.finalDamage

        if (damager is LivingEntity) {
            val item = damager.equipment?.itemInMainHand ?: return
            val meta = item.itemMeta ?: return
            if (!meta.hasEnchant(this)) return
            if (damage <= 0) return
            val level = meta.getEnchantLevel(this)

            doHitAction(event.entity, damager, level, event)
        } else if (damager is AbstractArrow) {
            val level = damager.getEnchantLevel(this) ?: return
            val shooter = damager.shooter as? LivingEntity ?: return

            doHitAction(event.entity, shooter, level, event)
        }

    }

    @EventHandler
    fun onShootArrow(event: EntityShootBowEvent) {
        val arrow = event.projectile as? Arrow ?: return

        val item = event.bow ?: return
        val meta = item.itemMeta ?: return
        if (!meta.hasEnchant(this)) return
        val level = meta.getEnchantLevel(this)

        arrow.addEnchantment(this, level)
    }
}