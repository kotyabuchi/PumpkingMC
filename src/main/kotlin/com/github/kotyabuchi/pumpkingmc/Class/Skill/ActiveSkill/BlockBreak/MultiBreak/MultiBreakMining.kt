package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak

import org.bukkit.Material

object MultiBreakMining: MultiBreak() {
    init {
        Material.values().forEach {
            if (it.name.endsWith("_PICKAXE")) addTool(it)
        }
    }
}