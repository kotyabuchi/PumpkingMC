package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

interface ActiveSkillMaster: Listener {
    val skillName: String
    val cost: Int
    val coolTime: Long
    val needLevel: Int
    val description: String
    val hasActiveTime: Boolean
    val activePlayers: MutableMap<UUID, BukkitTask>
    val coolTimePlayers: MutableList<UUID>

    fun calcActiveTime(level: Int): Int

    fun isEnabledSkill(uuid: UUID): Boolean = activePlayers.contains(uuid)

    fun enableAction(player: Player, level: Int)

    fun disableAction(player: Player)

    fun enableSkill(player: Player, level: Int) {
        val uuid = player.uniqueId
        if (needLevel > level) {
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
            player.sendActionBar('&', "&cNot enough levels (Need Lv.$needLevel)")
        } else {
            if (isEnabledSkill(uuid)) {
                disableSkill(player)
            } else {
                enableAction(player, level)
                if (hasActiveTime) {
                    activePlayers[uuid] = object : BukkitRunnable() {
                        override fun run() {
                            disableSkill(player)
                        }
                    }.runTaskLater(instance, calcActiveTime(level).toLong())
                }

                player.sendActionBar('&', "&a$skillName Enabled")
            }
        }
    }

    fun disableSkill(player: Player) {
        disableAction(player)
        activePlayers[player.uniqueId]?.cancel()
        activePlayers.remove(player.uniqueId)

        player.sendActionBar('&', "&c$skillName Disabled")
    }

    fun startCoolTime(uuid: UUID) {
        object : BukkitRunnable() {
            override fun run() {
                coolTimePlayers.remove(uuid)
            }
        }.runTaskLater(instance, coolTime)
    }
}