package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.Utility.hasTag
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
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
    val activePlayerLevelMap: MutableMap<UUID, Int>
    val activeTimeMap: MutableMap<UUID, BukkitTask>
    val coolTimePlayers: MutableList<UUID>

    fun getSkillNamespacedKey(): NamespacedKey = NamespacedKey(instance, skillName)

    fun calcActiveTime(level: Int): Int

    fun isEnabledSkill(player: Player): Boolean = player.hasTag(skillName)

    fun enableAction(player: Player, level: Int)

    fun disableAction(player: Player)

    fun toggleSkill(player: Player, level: Int) {
        if (isEnabledSkill(player)) {
            disableSkill(player)
        } else {
            enableSkill(player, level)
        }
    }

    fun enableSkill(player: Player, level: Int) {
        val uuid = player.uniqueId
        if (needLevel > level) {
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
            player.sendActionBar('&', "&cNot enough levels (Need Lv.$needLevel)")
        } else if (!isEnabledSkill(player)) {
            enableAction(player, level)
            activePlayerLevelMap[uuid] = level
            player.persistentDataContainer.set(getSkillNamespacedKey(), PersistentDataType.BYTE, 1)
            if (hasActiveTime) {
                activeTimeMap[uuid] = object : BukkitRunnable() {
                    override fun run() {
                        disableSkill(player)
                    }
                }.runTaskLater(instance, calcActiveTime(level).toLong())
            }
        }
    }

    fun disableSkill(player: Player) {
        val uuid = player.uniqueId
        disableAction(player)
        activeTimeMap[uuid]?.cancel()
        activeTimeMap.remove(uuid)
        activePlayerLevelMap.remove(uuid)
        player.persistentDataContainer.remove(getSkillNamespacedKey())
    }

    fun startCoolTime(uuid: UUID) {
        object : BukkitRunnable() {
            override fun run() {
                coolTimePlayers.remove(uuid)
            }
        }.runTaskLater(instance, coolTime)
    }
}