package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.ChangeHomeIconButton
import com.github.kotyabuchi.pumpkingmc.System.Player.Home
import com.github.kotyabuchi.pumpkingmc.System.Player.PlayerStatus
import org.bukkit.Material

class ChangeHomeIconMenu(private val home: Home, private val playerStatus: PlayerStatus): MenuBase("Change Home Icon", 6) {

    init {
        setFrame()

        var page = 0
        Material.values().forEach {
            if (!it.isAir) {
                if (getLastBlankSlot(page) == null) page++
                setMenuButton(ChangeHomeIconButton(home, playerStatus, it), page)
            }
        }
    }
}