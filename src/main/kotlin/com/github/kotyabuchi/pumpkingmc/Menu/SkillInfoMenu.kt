package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.MenuButtonBlank
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.SkillInfoButton
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase
import kotlin.math.ceil

class SkillInfoMenu(jobClass: JobClassMaster): MenuBase(jobClass.jobClassName.upperCamelCase() + " - Info", ceil(jobClass.getSkills().size / 9.0).toInt()) {

    init {
        setFrame()
        jobClass.getSkills().forEach { (skillCommand, skill) ->
            setMenuButton(SkillInfoButton(skill, skillCommand))
        }
        while (getLastBlankSlot() != null) {
            setMenuButton(MenuButtonBlank())
        }
    }
}