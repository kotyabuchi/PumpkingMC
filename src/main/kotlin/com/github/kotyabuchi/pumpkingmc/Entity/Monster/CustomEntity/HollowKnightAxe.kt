package com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity

import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.createHead
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Vindicator
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object HollowKnightAxe {

    fun spawn(location: Location) {
        if (location.isWorldLoaded) {
            val shadowAxe = location.world?.spawnEntity(location, EntityType.VINDICATOR) as? Vindicator ?: return
            val equipment = shadowAxe.equipment ?: return
            equipment.setItemInMainHand(ItemStack(Material.IRON_AXE))
            equipment.itemInMainHandDropChance = 0f
            equipment.helmetDropChance = 0f
            equipment.helmet = createHead("Jack", "Jack", "b89788b1-f84f-4e54-854d-cc84ede0d3d3",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2I2ODY1ZmE3YTJhMTc3NTk3NzQzYmUyMzAzZDY4YmRlZTYzNjBhZWEyOTQ1YzQ3MWQ4MjU1Y2JlNDYifX19")
            equipment.chestplate = null
            equipment.leggings = null
            equipment.boots = null
            shadowAxe.isSilent = true
            shadowAxe.customName = "&kKnight&r".colorS()
            shadowAxe.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 1, false, false))
            shadowAxe.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.32
        }
    }
}
