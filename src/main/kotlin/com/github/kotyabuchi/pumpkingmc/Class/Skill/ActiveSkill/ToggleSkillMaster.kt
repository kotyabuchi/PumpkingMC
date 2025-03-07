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

interface ToggleSkillMaster: Listener {
    val skillName: String
    val cost: Int
    val needLevel: Int
    val description: String

    fun getSkillNamespacedKey(): NamespacedKey = NamespacedKey(instance, skillName)

    fun isEnabledSkill(player: Player): Boolean = player.hasTag(getSkillNamespacedKey(), PersistentDataType.INTEGER)

    fun setSkillLevel(player: Player, level: Int) {
        player.persistentDataContainer.set(getSkillNamespacedKey(), PersistentDataType.INTEGER, level)
    }

    fun removeSkillLevel(player: Player) {
        player.persistentDataContainer.remove(getSkillNamespacedKey())
    }

    fun getSkillLevel(player: Player): Int? {
        return player.persistentDataContainer.get(getSkillNamespacedKey(), PersistentDataType.INTEGER)
    }

    fun enableAction(player: Player, level: Int) {
        player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        player.sendActionBar(Component.text("$skillName On", NamedTextColor.GREEN))
    }

    fun disableAction(player: Player) {
        player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        player.sendActionBar(Component.text("$skillName Off", NamedTextColor.RED))
    }

    fun toggleSkill(player: Player, level: Int) {
        if (isEnabledSkill(player)) {
            disableSkill(player)
        } else {
            enableSkill(player, level)
        }
    }

    fun enableSkill(player: Player, level: Int) {
        if (needLevel > level) {
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
            player.sendActionBar(Component.text("$skillName: Not enough levels (Need Lv.$needLevel)").color(NamedTextColor.RED))
        } else if (!isEnabledSkill(player)) {
            enableAction(player, level)
            setSkillLevel(player, level)
        }
    }

    fun disableSkill(player: Player) {
        disableAction(player)
        removeSkillLevel(player)
    }
}