package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak

import com.github.kotyabuchi.pumpkingmc.Enum.ToolType
import org.bukkit.Material

object MultiBreakExcavation: MultiBreak() {

    override val type: String = "Excavation"

    init {
        Material.values().forEach {
            if (ToolType.SHOVEL.includes(it)) addTool(it)
        }
    }
}