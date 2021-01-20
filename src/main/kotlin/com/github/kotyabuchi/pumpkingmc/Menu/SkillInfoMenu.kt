package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Utility.beginWithUpperCase

class SkillInfoMenu(jobClassType: JobClassType): MenuBase(jobClassType.name.beginWithUpperCase() + " - Info", 1) {

}