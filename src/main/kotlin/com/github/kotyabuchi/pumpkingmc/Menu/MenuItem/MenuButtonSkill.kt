package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag

class MenuButtonSkill(jobClassType: JobClassType, player: Player): MenuButtonBase() {

    init {
        val jobClassStatus = player.getStatus().getJobClassStatus(jobClassType.jobClass)
        val lore = mutableListOf<String>()
        lore.add("Level: ${jobClassStatus.getLevel()}")
        lore.add("Need Exp: ${jobClassStatus.getExp().floor2Digits()}/${jobClassStatus.getNextLevelExp()}")
        lore.add("Total Exp: ${jobClassStatus.getTotalExp().floor2Digits()}")
        lore.add("")
        menuItem = ItemStackGenerator(jobClassType.getIcon()).setDisplayName(jobClassType.name.upperCamelCase()).setFlag(ItemFlag.HIDE_ATTRIBUTES).setLore(lore).setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {

    }
}