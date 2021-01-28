package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object Vitality: JobClassMaster(JobClassType.VITALITY) {

    init {
        instance.server.onlinePlayers.forEach {
            setHealth(it)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerLoginEvent) {
        object : BukkitRunnable() {
            override fun run() {
                setHealth(event.player)
            }
        }.runTaskLater(instance, 20)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val playerStatus = player.getStatus()
        val level = playerStatus.getJobClassStatus(jobClassType).getLevel()
        val cause = event.cause
        if (cause == EntityDamageEvent.DamageCause.FALL ||
                cause == EntityDamageEvent.DamageCause.LAVA ||
                cause == EntityDamageEvent.DamageCause.DROWNING ||
                cause == EntityDamageEvent.DamageCause.DRYOUT ||
                cause == EntityDamageEvent.DamageCause.VOID ||
                cause == EntityDamageEvent.DamageCause.STARVATION ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.MELTING ||
                cause == EntityDamageEvent.DamageCause.HOT_FLOOR ||
                cause == EntityDamageEvent.DamageCause.CRAMMING ||
                cause == EntityDamageEvent.DamageCause.CONTACT ||
                cause == EntityDamageEvent.DamageCause.SUICIDE) return
        val amount = event.damage
        if (amount > 0) {
            playerStatus.addSkillExp(jobClassType, amount * 2)

            // Battle Healing -
            val battleHealingChance = min(50, max(500, level / 2))
            val battleHealingLevel = 1 + floor(level / 250.0).toInt()

            if (Random.nextInt(1000) <= battleHealingChance) {
                player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 20 * 3, battleHealingLevel, true, true))
                player.sendActionMessage("&c♡Battle Healing♡")
            }
            // - Battle Healing
        }
        event.damage = event.damage - level / 50.0
    }

    @EventHandler
    fun onHeal(event: EntityRegainHealthEvent) {
        val player = event.entity as? Player ?: return
        val playerStatus = player.getStatus()
        event.amount = event.amount * (1 + playerStatus.getJobClassStatus(jobClassType).getLevel() / 100.0)
        val amount = event.amount
        if (amount > 0) playerStatus.addSkillExp(jobClassType, amount)
    }

    override fun levelUpEvent(player: Player) {
        setHealth(player)
    }

    private fun setHealth(player: Player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.let {
            val newHealth = it.defaultValue + player.getStatus().getJobClassStatus(jobClassType).getLevel() / 10.0
            it.baseValue = newHealth
        }
    }
}