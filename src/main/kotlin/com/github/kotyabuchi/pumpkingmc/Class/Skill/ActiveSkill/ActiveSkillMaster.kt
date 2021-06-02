package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.Utility.floor1Digits
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

interface ActiveSkillMaster: ToggleSkillMaster {
    val coolTime: Long
    val hasActiveTime: Boolean
    val activeTimeMap: MutableMap<UUID, BukkitTask>
    val lastUseTime: MutableMap<UUID, Long>

    fun calcActiveTime(level: Int): Int

    override fun enableSkill(player: Player, level: Int) {
        val uuid = player.uniqueId
        if (needLevel > level) {
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
            player.sendActionBar(
                Component.text("$skillName: Not enough levels (Need Lv.$needLevel)").color(NamedTextColor.RED))
        } else if (isReadySkill(uuid)) {
            player.sendActionBar(Component.text("$skillName: Not yet (${(getRemainingCoolTime(uuid) / 1000.0).floor1Digits()}s)").color(NamedTextColor.RED))
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

    override fun disableSkill(player: Player) {
        val uuid = player.uniqueId
        disableAction(player)
        activeTimeMap[uuid]?.cancel()
        activeTimeMap.remove(uuid)
        activePlayerLevelMap.remove(uuid)
        player.persistentDataContainer.remove(getSkillNamespacedKey())
    }

    fun setLastUseTime(uuid: UUID) {
        lastUseTime[uuid] = System.currentTimeMillis()
    }

    fun getRemainingCoolTime(uuid: UUID): Long {
        return lastUseTime[uuid]?.let { coolTime - (System.currentTimeMillis() - it) } ?: 0
    }

    fun isReadySkill(uuid: UUID): Boolean {
        return getRemainingCoolTime(uuid) <= 0L
    }

    fun restartActiveTime(player: Player, level: Int) {
        val uuid = player.uniqueId
        activeTimeMap[uuid]?.cancel()
        activeTimeMap[uuid] = object : BukkitRunnable() {
            override fun run() {
                disableSkill(player)
            }
        }.runTaskLater(instance, calcActiveTime(level).toLong())
    }
}