package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.BlankButton
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.JobClassButton
import org.bukkit.entity.Player
import kotlin.math.ceil

class SkillMenu(player: Player): MenuBase("Skill", ceil(JobClassType.values().size / 7.0).toInt()) {

    init {
        setFrame()
        JobClassType.values().forEach {
            setMenuButton(JobClassButton(it, player))
        }
        while (getLastBlankSlot() != null) {
            setMenuButton(BlankButton())
        }
    }
}