package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak

import com.github.kotyabuchi.pumpkingmc.Enum.ToolType
import org.bukkit.Material

object MultiBreakMining: MultiBreak() {

    override val type: String = "Mining"

    init {
        Material.values().forEach {
            if (ToolType.PICKAXE.includes(it)) addTool(it)
        }
    }
}