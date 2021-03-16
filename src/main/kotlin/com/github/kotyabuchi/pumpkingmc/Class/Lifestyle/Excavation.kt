package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.GroundLevelingAssist
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak.MultiBreakMining
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getJobClassLevel
import org.bukkit.Material
import org.bukkit.entity.Player

object Excavation: BlockBreakJobClass("EXCAVATION") {

    private val dirtSet = setOf(Material.DIRT, Material.SAND, Material.GRASS_BLOCK, Material.GRAVEL, Material.FARMLAND,
        Material.GRASS_PATH, Material.COARSE_DIRT, Material.PODZOL, Material.RED_SAND, Material.SOUL_SAND, Material.SOUL_SOIL)

    private val groundLevelingAssist = GroundLevelingAssist(this)

    init {
        Material.values().forEach {
            if (it.name.endsWith("_SHOVEL")) addTool(it)
        }
        dirtSet.forEach {
            addExpMap(it, exp = 1)
            if (!it.hasGravity()) groundLevelingAssist.addAssistBlock(it)
        }
        addExpMap(Material.CLAY, exp = 2)

        addAction(SkillCommand.LLR, 100, fun(player: Player) {
            MultiBreakMining.toggleSkill(player, player.getJobClassLevel(this))
        })
        addAction(SkillCommand.LRR, 200, fun(player: Player) {
            groundLevelingAssist.toggleSkill(player, player.getJobClassLevel(this))
        })
    }
}
