package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.MenuButtonHome
import com.github.kotyabuchi.pumpkingmc.System.Player.Home
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.removeHome
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class HomeMenu(player: Player, private val remove: Boolean = false): MenuBase(if (remove) ("${player.name}'s Homes &4[Remove]").colorS() else "${player.name}'s Homes".colorS(), 4) {

    init {
        setFrame()

        var page = 0
        player.bedSpawnLocation?.let { loc ->
            loc.world?.let { world ->
                setMenuButton(MenuButtonHome(Home(null, "Bed", world, loc.x, loc.y, loc.z, 0f, Material.RED_BED)), page)
            }
        }
        player.getStatus().homes.forEach {
            if (getLastBlankSlot(page) == null) page++
            setMenuButton(MenuButtonHome(it), page)
        }
    }

    override fun doButtonClickEvent(slot: Int, event: InventoryClickEvent, page: Int) {
        createPageIfNeed(page)
        val button = getButton(slot, page)
        if (button is MenuButtonHome) {
            val player = event.whoClicked as? Player ?: return
            playClickedButtonSound(button, player)
            val status = player.getStatus()
            val home = button.home
            if (event.isLeftClick) {
                if (remove) {
                    val homeId = home.homeId ?: return
                    removeHome(homeId)
                    status.homes.remove(home)
                    status.openMenu(HomeMenu(player, true), 0, true)
                } else {
                    getButton(slot, page)?.clickEvent(event)
                }
            } else if (event.isRightClick) {
                if (remove) {

                } else {
                    status.openMenu(ChangeHomeIconMenu(home, status))
                }
            }
        } else {
            super.doButtonClickEvent(slot, event, page)
        }
    }
}