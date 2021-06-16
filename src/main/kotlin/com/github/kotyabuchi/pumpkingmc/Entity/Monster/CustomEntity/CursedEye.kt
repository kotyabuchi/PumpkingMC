package com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity

import com.github.kotyabuchi.pumpkingmc.Utility.createHead
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Enderman
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Vex
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object CursedEye: Listener {

    init {
        instance.registerEvent(this)
    }

    fun spawn(owner: Enderman) {
        val eye = owner.world.spawn(owner.location.add(.0, 1.0, .0), Vex::class.java) { eye ->
            eye.isSilent = true
            eye.customName = "Cursed Eye"
            eye.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 1, false, false))
            eye.addPotionEffect(PotionEffect(PotionEffectType.SLOW, Int.MAX_VALUE, 1, false, false))
            eye.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 1.0
            eye.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 1.0
            eye.health = 1.0

            eye.equipment?.helmetDropChance = 0f
            eye.equipment?.helmet = createHead("Cursed Eye", "Eye of Ender", "36122cdc-6c97-4b97-990a-ef4df57db922",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhOGZjOGRlNjQxN2I0OGQ0OGM4MGI0NDNjZjUzMjZlM2Q5ZGE0ZGJlOWIyNWZjZDQ5NTQ5ZDk2MTY4ZmMwIn19fQ==")
            eye.equipment?.setItemInMainHand(null)
        }
        eye.target = owner.target
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        val damager = event.damager as? Vex ?:return
        if (damager.customName == "Cursed Eye") {
            event.isCancelled = true
            entity.damage(1.0)
            entity.noDamageTicks = 0
        }
    }
}