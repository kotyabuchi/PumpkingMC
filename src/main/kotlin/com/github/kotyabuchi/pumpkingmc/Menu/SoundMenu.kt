package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.PlaySoundButton
import org.bukkit.Sound
import kotlin.math.floor

class SoundMenu: MenuBase("Sound Sample", 4) {

    init {
        setFrame()
        Sound.values().forEachIndexed { index, sound ->
            val page =  floor(index.toDouble() / getSlotAmount()).toInt()
            setMenuButton(PlaySoundButton(sound), page = page)
        }
    }
}