package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.MenuButtonBlank
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.MenuButtonSkill
import org.bukkit.entity.Player
import kotlin.math.ceil

class SkillMenu(player: Player): MenuBase("Skill", ceil(JobClassType.values().size / 7.0).toInt()) {

    init {
        setFrame()
        JobClassType.values().forEach {
            setMenuButton(MenuButtonSkill(it, player))
        }
        while (getLastBlankSlot() != null) {
            setMenuButton(MenuButtonBlank())
        }
    }
}