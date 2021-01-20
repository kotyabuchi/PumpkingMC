package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.ceil
import kotlin.random.Random

class DamagePopup: Listener {

    private val popupStands = mutableListOf<ArmorStand>()

    @EventHandler(priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageEvent) {
        if (event.isCancelled) return
        val entity = event.entity as? LivingEntity ?: return
        if (entity !is Player) {
            var needPopup = false
            val nearEntities = entity.getNearbyEntities(30.0, 30.0, 30.0)
            for (i in 0 until nearEntities.size) {
                val checkTarget = nearEntities[i]
                if (checkTarget is Player) {
                    needPopup = true
                    break
                }
            }
            if (!needPopup) return
        }
        val eyeHeight = entity.eyeHeight
        val baseLoc = entity.location.add(.0, eyeHeight, .0)
        val x = Random.nextInt(15) / 10.0 - .75
        val y = Random.nextInt(ceil((eyeHeight / 4.0) * 10).toInt()) / 10.0
        val z = Random.nextInt(15) / 10.0 - .75
        val popupLoc = baseLoc.add(x, y, z)
        val stand = EntityType.ARMOR_STAND.entityClass?.let { entity.world.spawn(popupLoc, it) {
            if (it is ArmorStand) {
                it.isVisible = false
                it.isMarker = true
                it.isSmall = true
                it.isSilent = true
                it.setGravity(false)
                it.setAI(false)
                it.isCustomNameVisible = true
                it.customName = "&c${event.finalDamage.floor2Digits()}".colorS()
            }
        }} as ArmorStand
        object : BukkitRunnable() {
            override fun run() {
                stand.remove()
            }
        }.runTaskLater(instance, 30)
        popupStands.add(stand)
    }

    fun clearPopup() {
        popupStands.forEach {
            it.remove()
        }
    }
}