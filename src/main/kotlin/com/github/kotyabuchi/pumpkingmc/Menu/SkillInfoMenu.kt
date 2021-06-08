package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase

class SkillInfoMenu(jobClassType: JobClassType): MenuBase(jobClassType.name.upperCamelCase() + " - Info", 1) {

}