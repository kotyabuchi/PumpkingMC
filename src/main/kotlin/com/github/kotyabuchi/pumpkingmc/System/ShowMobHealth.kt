package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Utility.beginWithUpperCase
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.max

class ShowMobHealth: Listener {

    private val healthBarMap: MutableMap<Player, BukkitTask> = mutableMapOf()

    @EventHandler(priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        var damager = event.damager
        if (damager !is Player) {
            if (damager is Arrow && damager.shooter is Player) {
                damager = damager.shooter as Player
            } else return
        }
        val mob = event.entity as? LivingEntity ?: return
        val name = if (mob is Player) mob.name else mob.customName ?: mob.type.name.beginWithUpperCase()
        val health = max(0.0, mob.health - event.finalDamage).floor2Digits()
        val maxHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: return

        healthBarMap[damager]?.cancel()
        healthBarMap.remove(damager)

        val bossBarKey = NamespacedKey(instance, "HEALTH_BAR_${damager.uniqueId}")
        val bossBar = instance.server.getBossBar(bossBarKey) ?: instance.server.createBossBar(bossBarKey, "", BarColor.RED, BarStyle.SOLID)
        bossBar.apply {
            setTitle("$name ($health / $maxHealth) &c-${event.finalDamage.floor2Digits()}".colorS())
            removeAll()
            addPlayer(damager)
            isVisible = true
            progress = if (health == 0.0) health else max(0.0, health / maxHealth)
        }

        healthBarMap[damager] = object : BukkitRunnable() {
            override fun run() {
                bossBar.removeAll()
                bossBar.isVisible = false
            }
        }.runTaskLater(instance, 20 * 6)
    }
}