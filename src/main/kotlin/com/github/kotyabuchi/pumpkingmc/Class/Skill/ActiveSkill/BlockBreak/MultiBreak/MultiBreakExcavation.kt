package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak

import org.bukkit.Material

object MultiBreakExcavation: MultiBreak() {
    init {
        Material.values().forEach {
            if (it.name.endsWith("_SHOVEL")) MultiBreakMining.addTool(it)
        }
    }
}