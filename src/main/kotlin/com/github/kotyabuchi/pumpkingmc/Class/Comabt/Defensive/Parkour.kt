package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.math.min
import kotlin.random.Random

object Parkour: JobClassMaster("PARKOUR") {

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val playerStatus = player.getStatus()
        val level = playerStatus.getJobClassStatus(this).getLevel()
        val cause = event.cause
        if (cause == EntityDamageEvent.DamageCause.FALL) {
            val expAmount = event.finalDamage
            if (Random.nextInt(1000) < min(1000, level) / 2) {
                event.damage -= 4
                player.sendActionMessage("&9*Rolling!*")
                player.playSound(player.location, Sound.BLOCK_PISTON_CONTRACT, 0.2f, 2f)
            }
            if (player.isSneaking && Random.nextInt(1000) < min(2000, level) / 5) {
                event.damage -= 6
                player.sendActionMessage("&b**Graceful Rolling!**")
                player.playSound(player.location, Sound.BLOCK_PISTON_CONTRACT, 0.2f, 2f)
            }
            if (Random.nextInt(1000) < min(1000, level) / 5) {
                event.damage = .0
                player.sendActionMessage("&6***Hero landing!***")
                player.playSound(player.location, Sound.BLOCK_PISTON_CONTRACT, 0.2f, 2f)
            }
            playerStatus.addSkillExp(this, expAmount)
        }
    }
}