package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantment
import org.bukkit.Location
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.persistence.PersistentDataType

fun Entity.jump(loc: Location) {
    val entityLoc = this.location
    val targetLoc = loc.add(.0, entityLoc.distance(loc) / 40.0, .0).toVector()
    val vel = targetLoc.clone().subtract(entityLoc.toVector()).multiply(0.3)
    if (vel.x > 4.0) {
        vel.x = 4.0
    } else if (vel.x < -4.0) {
        vel.x = -4.0
    }
    if (vel.y > 4.0) {
        vel.y = 4.0
    } else if (vel.y < -4.0) {
        vel.y = -4.0
    }
    if (vel.z > 4.0) {
        vel.z = 4.0
    } else if (vel.z < -4.0) {
        vel.z = -4.0
    }
    this.velocity = vel
}

fun Projectile.addEnchantment(enchant: Enchantment, level: Int) {
    val pdc = this.persistentDataContainer
    var enchantments = pdc.get(CustomEnchantment.EnchantmentKey, PersistentDataType.STRING)
    if (enchantments == null) enchantments = "" else enchantments += ","
    pdc.set(CustomEnchantment.EnchantmentKey, PersistentDataType.STRING, "$enchantments${enchant.key.value()}:$level")
}

fun Projectile.hasEnchantment(enchant: Enchantment): Boolean {
    val pdc = this.persistentDataContainer
    val enchantments = pdc.get(CustomEnchantment.EnchantmentKey, PersistentDataType.STRING)?.split(",") ?: return false
    enchantments.forEach {
        if (it.split(":")[0] == enchant.key.value()) return true
    }
    return false
}

fun Projectile.getEnchantLevel(enchant: Enchantment): Int? {
    val pdc = this.persistentDataContainer
    val enchantments = pdc.get(CustomEnchantment.EnchantmentKey, PersistentDataType.STRING)?.split(",") ?: return null
    enchantments.forEach {
        val enchantStatus = it.split(":")
        if (enchantStatus[0] == enchant.key.value()) return Integer.parseInt(enchantStatus[1])
    }
    return null
}