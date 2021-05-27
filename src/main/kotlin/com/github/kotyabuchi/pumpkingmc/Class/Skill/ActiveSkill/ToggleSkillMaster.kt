package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.Utility.hasTag
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import java.util.*

interface ToggleSkillMaster: Listener {
    val skillName: String
    val cost: Int
    val needLevel: Int
    val description: String
    val activePlayerLevelMap: MutableMap<UUID, Int>

    fun getSkillNamespacedKey(): NamespacedKey = NamespacedKey(instance, skillName)

    fun calcActiveTime(level: Int): Int

    fun isEnabledSkill(player: Player): Boolean = player.hasTag(getSkillNamespacedKey())

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
            player.sendActionBar(Component.text("$skillName: Not enough levels (Need Lv.$needLevel)").color(NamedTextColor.RED))
        } else if (!isEnabledSkill(player)) {
            enableAction(player, level)
            activePlayerLevelMap[uuid] = level
            player.persistentDataContainer.set(getSkillNamespacedKey(), PersistentDataType.BYTE, 1)
        }
    }

    fun disableSkill(player: Player) {
        val uuid = player.uniqueId
        disableAction(player)
        activePlayerLevelMap.remove(uuid)
        player.persistentDataContainer.remove(getSkillNamespacedKey())
    }
}