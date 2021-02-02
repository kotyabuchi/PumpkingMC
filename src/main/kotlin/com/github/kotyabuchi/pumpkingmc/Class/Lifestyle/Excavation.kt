package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.Utility.toggleTag
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

object Excavation: BlockBreakJobClass("EXCAVATION") {

    private val dirtSet = setOf(Material.DIRT, Material.SAND, Material.GRASS_BLOCK, Material.GRAVEL, Material.FARMLAND,
        Material.GRASS_PATH, Material.COARSE_DIRT, Material.PODZOL, Material.RED_SAND, Material.SOUL_SAND, Material.SOUL_SOIL)

    init {
        Material.values().forEach {
            if (it.name.endsWith("_SHOVEL")) addTool(it)
        }
        dirtSet.forEach {
            addExpMap(it, exp = 1)
            if (!it.hasGravity()) addGroundLevelingAssist(it)
        }
        addExpMap(Material.CLAY, exp = 2)

        addAction(SkillCommand.RRR, 25, fun(player: Player) {
            SuperBreaker.enableSuperBreaker(player, this)
        })
        addAction(SkillCommand.LLR, 100, fun(player: Player) {
            if (player.toggleTag(multiBreakKey)) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aMulti Break On")
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cMulti Break Off")
            }
        })
        addAction(SkillCommand.LRR, 200, fun(player: Player) {
            if (player.toggleTag(groundLevelingAssistKey)) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aGround Leveling Assist On")
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cGround Leveling Assist Off")
            }
        })
    }
}
