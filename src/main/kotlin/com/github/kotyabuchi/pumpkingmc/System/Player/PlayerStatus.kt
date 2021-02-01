package com.github.kotyabuchi.pumpkingmc.System.Player

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Menu.MenuBase
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.floor3Digits
import com.github.kotyabuchi.pumpkingmc.Utility.savePlayerStatus
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

data class PlayerStatus(val player: Player) {

    private var openingMenu: MenuBase? = null
    private var openingMenuPage: Int = 0
    var openMenuWithCloseMenu = false

    val homes = mutableListOf<Home>()

    private val jobClassStatusMap = mutableMapOf<JobClassType, JobClassStatus>()
    private val expBarMap = mutableMapOf<JobClassType, BukkitTask>()

    fun closeMenu() {
        openingMenu?.doCloseMenuAction(player)
        openingMenuPage = 0
        openingMenu = null
    }

    fun setOpeningMenu(menu: MenuBase) {
        openingMenu = menu
    }

    fun getOpeningMenu(): MenuBase? = openingMenu

    fun getOpeningPage(): Int = openingMenuPage

    fun backPage() {
        openingMenu?.let {
            openMenu(it, openingMenuPage - 1)
        }
    }

    fun nextPage() {
        openingMenu?.let {
            openMenu(it, openingMenuPage + 1)
        }
    }

    fun openMenu(menu: MenuBase, page: Int = 0, prev: Boolean = false) {
        if (openingMenu != null && openingMenu != menu) {
            if (!prev) menu.setPrevMenu(openingMenu!!)
            openMenuWithCloseMenu = true
        }
        player.openInventory(menu.getInventory(page))
        openingMenu = menu
        openingMenuPage = page
    }

    fun noticeLevelUp(jobClassType: JobClassType) {
        val jobClassStatus = getJobClassStatus(jobClassType)
        player.sendTitle("Level Up!", "${jobClassType.regularName} Lv. ${jobClassStatus.getLevel()}", 10, 20 * 2, 10)
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 1.0f)
    }

    fun addSkillExp(jobClassType: JobClassType, point: Double, increaseCombo: Int = 1) {
        val jobClassStatus = getJobClassStatus(jobClassType)
        if (jobClassStatus.addExp(point, increaseCombo) == JobClassStatus.AddExpResult.LEVEL_UP) noticeLevelUp(jobClassType)
        val addedExp = jobClassStatus.getRecentAddedExp()
        val combo = jobClassStatus.getCombo()
        val skillName = jobClassType.regularName

        if (expBarMap.containsKey(jobClassType)) {
            expBarMap[jobClassType]!!.cancel()
            expBarMap.remove(jobClassType)
        }
        val exp = jobClassStatus.getExp()
        val nextLevelExp = jobClassStatus.getNextLevelExp()
        var title = "$skillName Lv.${jobClassStatus.getLevel()} ${exp.floor2Digits()}/$nextLevelExp"
        title += if (addedExp > 0) " &a+${addedExp.floor2Digits()} " else " &c+${addedExp.floor2Digits()} "
        title += " &6${combo}Combo(x${(1 + combo * 0.002).floor3Digits()})"
        val bossBar = instance.server.getBossBar(jobClassType.getExpBossBarKey(player)) ?: instance.server.createBossBar(jobClassType.getExpBossBarKey(player), title, BarColor.GREEN, BarStyle.SEGMENTED_10)
        bossBar.apply {
            isVisible = true
            addPlayer(player)
            setTitle(title.colorS())
            progress = exp / nextLevelExp
        }
        expBarMap[jobClassType] = object : BukkitRunnable() {
            override fun run() {
                jobClassStatus.resetCombo()
                bossBar.removeAll()
                bossBar.isVisible = false
            }
        }.runTaskLater(instance, 20 * 6)
        setJobClassStatus(jobClassType, jobClassStatus)
    }

    fun setJobClassStatus(jobClassType: JobClassType, jobClassStatus: JobClassStatus) {
        jobClassStatusMap[jobClassType] = jobClassStatus
    }

    fun getJobClassStatus(jobClassType: JobClassType): JobClassStatus {
        return jobClassStatusMap[jobClassType] ?: JobClassStatus()
    }

    fun save() {
        savePlayerStatus(this)
    }
}