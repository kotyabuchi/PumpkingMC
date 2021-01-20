package com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity

import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Slime
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object SlimeSkeleton {

    fun ride(skeleton: Skeleton, rideMob: EntityType = EntityType.SLIME) {
        val slime = skeleton.world.spawnEntity(skeleton.location, rideMob) as? Slime ?: return
        slime.size = 2
        slime.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 2, false, false))
        slime.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.let {
            it.baseValue = it.defaultValue + 0.5
        }
        slime.addPassenger(skeleton)
    }
}