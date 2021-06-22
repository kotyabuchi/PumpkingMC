package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Menu.SkillInfoMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.floor1Digits
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import kotlin.math.round

class JobClassButton(private val jobClassType: JobClassType, player: Player): MenuButtonBase() {

    private val jobClass = jobClassType.jobClass

    init {
        val jobClassStatus = player.getStatus().getJobClassStatus(jobClassType.jobClass)
        val lore = mutableListOf<String>()
        lore.add("Level: ${jobClassStatus.getLevel()}")
        val exp = jobClassStatus.getExp().floor2Digits()
        val nextLevel = jobClassStatus.getNextLevelExp()
        lore.add("Need Exp: $exp/$nextLevel [${(round(exp / nextLevel * 1000) / 10).floor1Digits()}%]")
        lore.add("Total Exp: ${jobClassStatus.getTotalExp().floor2Digits()}")
        menuItem = ItemStackGenerator(jobClassType.getIcon()).setDisplayName(jobClassType.name.upperCamelCase()).setFlag(ItemFlag.HIDE_ATTRIBUTES).setLore(lore).setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        if (jobClass.getSkills().isNotEmpty()) (event.whoClicked as? Player)?.getStatus()?.openMenu(SkillInfoMenu(jobClassType.jobClass))
    }
}