package com.github.kotyabuchi.pumpkingmc.Class

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

open class JobClassMaster(val jobClassType: JobClassType): Listener {

    val name = jobClassType.regularName

    private val targetTool: MutableList<Material> = mutableListOf()
    private val castingModeList: MutableList<Player> = mutableListOf()
    private val castingCommandMap: MutableMap<Player, String> = mutableMapOf()
    private val skillMap: MutableMap<SkillCommand, (player: Player) -> Unit> = mutableMapOf()
    private val needLevelMap: MutableMap<SkillCommand, Int> = mutableMapOf()

    @EventHandler
    fun modeChange(event: PlayerSwapHandItemsEvent) {
        if (!targetTool.contains(event.offHandItem?.type)) return
        event.isCancelled = true
        val player = event.player
        if (castingModeList.contains(player)) {
            activeSkill(player)
            castingModeList.remove(player)
            castingCommandMap.remove(player)
        } else {
            castingModeList.add(player)
            castingCommandMap[player] = ""
            player.sendActionBar('&', "&aCast Mode On")
            player.sendTitle("", "- - -", 0, 20 * 1, 10)
            player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        if (!castingModeList.contains(player)) return
        event.isCancelled = true
        if (event.hand != EquipmentSlot.HAND) return
        castingCommandMap[player]?.let {
            val thisTimeAction = if (event.action.name.startsWith("LEFT_CLICK")) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.3f)
                "L"
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 0.7f)
                "R"
            }
            val newActionStr = it + thisTimeAction
            castingCommandMap[player] = newActionStr
            var subTitle = newActionStr
            for (i in 0 until 3 - newActionStr.length) {
                subTitle += " &r-"
            }
            player.sendTitle("", subTitle.replace("L", " &aL").replace("R", " &cR").colorS(), 0, 20 * 1, 10)
            if (newActionStr.length == 3) {
                activeSkill(player)
                castingModeList.remove(player)
                castingCommandMap.remove(player)
            }
        }
    }
    
    @EventHandler
    fun onClick(event: PlayerItemHeldEvent) {
        val player = event.player
        if (!castingModeList.contains(player)) return
        castingModeList.remove(player)
        castingCommandMap.remove(player)
        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
        player.sendActionBar('&', "&cCast Mode canceled")
    }

    protected fun getTool(): List<Material> {
        return targetTool
    }
    
    protected fun addTool(tool: Material): JobClassMaster {
        targetTool.add(tool)
        return this
    }

    protected fun addAction(skillCommand: SkillCommand, needLevel: Int = 0, action: (player: Player) -> Unit) {
        skillMap[skillCommand] = action
        needLevelMap[skillCommand] = needLevel
    }

    private fun activeSkill(player: Player) {
        val castingAction = castingCommandMap[player] ?: return
        try {
            val skillCommand = SkillCommand.valueOf(castingAction)
            val action = skillMap[skillCommand]
            val needLevel = needLevelMap[skillCommand]
            if (action == null || needLevel == null) {
                notRegisterActionNotice(player)
            } else if (player.getStatus().getJobClassStatus(jobClassType).getLevel() < needLevel) {
                notEnoughLevelNotice(player, needLevel)
            } else {
                action.invoke(player)
            }
        } catch (e: IllegalArgumentException) {
            notRegisterActionNotice(player)
        }
    }

    private fun notRegisterActionNotice(player: Player) {
        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
        player.sendActionBar('&', "&cNot Registered Action")
    }

    private fun notEnoughLevelNotice(player: Player, level: Int) {
        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
        player.sendActionBar('&', "&cNot enough levels (Need Lv.$level)")
    }

    open fun levelUpEvent(player: Player) {}
}
