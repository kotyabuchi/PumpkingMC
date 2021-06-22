package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.ToolPartPatternButton
import kotlin.math.ceil

class SmithingPatternMenu: MenuBase("Tool Part Pattern", ceil(ToolPartType.values().size / 7.0).toInt()) {

    init {
        setFrame()
        ToolPartType.values().forEach {
            setMenuButton(ToolPartPatternButton(it))
        }
    }
}