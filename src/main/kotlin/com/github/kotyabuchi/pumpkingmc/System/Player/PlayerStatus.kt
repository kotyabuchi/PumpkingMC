package com.github.kotyabuchi.pumpkingmc.System.Player

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Menu.MenuBase
import com.github.kotyabuchi.pumpkingmc.Utility.*
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

    private val jobClassStatusMap = mutableMapOf<JobClassMaster, JobClassStatus>()
    private val expBarMap = mutableMapOf<JobClassMaster, BukkitTask>()

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

    fun noticeLevelUp(jobClass: JobClassMaster) {
        val jobClassStatus = getJobClassStatus(jobClass)
        player.sendTitle("Level Up!", "${jobClass.jobClassName} Lv. ${jobClassStatus.getLevel()}", 10, 20 * 2, 10)
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 1.0f)
    }

    fun addSkillExp(jocClass: JobClassMaster, point: Double, increaseCombo: Int = 1) {
        val jobClassStatus = getJobClassStatus(jocClass)
        if (jobClassStatus.addExp(point, increaseCombo) == JobClassStatus.AddExpResult.LEVEL_UP) noticeLevelUp(jocClass)
        val addedExp = jobClassStatus.getRecentAddedExp()
        val combo = jobClassStatus.getCombo()
        val skillName = jocClass.jobClassName.beginWithUpperCase()

        if (expBarMap.containsKey(jocClass)) {
            expBarMap[jocClass]!!.cancel()
            expBarMap.remove(jocClass)
        }
        val exp = jobClassStatus.getExp()
        val nextLevelExp = jobClassStatus.getNextLevelExp()
        var title = "$skillName Lv.${jobClassStatus.getLevel()} ${exp.floor2Digits()}/$nextLevelExp"
        title += if (addedExp > 0) " &a+${addedExp.floor2Digits()} " else " &c+${addedExp.floor2Digits()} "
        title += " &6${combo}Combo(x${(1 + combo * 0.002).floor3Digits()})"
        val bossBar = instance.server.getBossBar(jocClass.getExpBossBarKey(player)) ?: instance.server.createBossBar(jocClass.getExpBossBarKey(player), title, BarColor.GREEN, BarStyle.SEGMENTED_10)
        bossBar.apply {
            isVisible = true
            addPlayer(player)
            setTitle(title.colorS())
            progress = exp / nextLevelExp
        }
        expBarMap[jocClass] = object : BukkitRunnable() {
            override fun run() {
                jobClassStatus.resetCombo()
                bossBar.removeAll()
                bossBar.isVisible = false
            }
        }.runTaskLater(instance, 20 * 6)
        setJobClassStatus(jocClass, jobClassStatus)
    }

    fun setJobClassStatus(jobClass: JobClassMaster, jobClassStatus: JobClassStatus) {
        jobClassStatusMap[jobClass] = jobClassStatus
    }

    fun getJobClassStatus(jobClass: JobClassMaster): JobClassStatus {
        return jobClassStatusMap[jobClass] ?: JobClassStatus()
    }

    fun save() {
        savePlayerStatus(this)
    }
}