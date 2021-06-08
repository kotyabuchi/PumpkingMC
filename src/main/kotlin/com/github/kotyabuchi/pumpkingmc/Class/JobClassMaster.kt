package com.github.kotyabuchi.pumpkingmc.Class

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToggleSkillMaster
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getJobClassLevel
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.getHeadLocation
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

open class JobClassMaster(val jobClassName: String): Listener {

    fun getExpBossBarKey(player: Player): NamespacedKey = NamespacedKey(instance, this.jobClassName + "_ExpBar_" + player.uniqueId.toString())

    private val targetTool: MutableList<Material> = mutableListOf()
    private val castingModeList: MutableList<Player> = mutableListOf()
    private val castingCommandMap: MutableMap<Player, String> = mutableMapOf()
    private val skillMap: MutableMap<SkillCommand, ToggleSkillMaster> = mutableMapOf()

    @EventHandler
    fun modeChange(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (player.isSneaking) return
        if (!targetTool.contains(event.offHandItem?.type)) return
        event.isCancelled = true
        if (castingModeList.contains(player)) {
            activeSkill(player)
            castingModeList.remove(player)
            castingCommandMap.remove(player)
        } else {
            castingModeList.add(player)
            castingCommandMap[player] = ""
            player.sendActionBar(Component.text("Cast Mode Enabled", NamedTextColor.GREEN))
            player.sendTitle("", "- - -", 0, 20 * 1, 10)
            player.world.playSound(player.getHeadLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        if (!castingModeList.contains(player)) return
        event.isCancelled = true
        if (event.hand != EquipmentSlot.HAND) return
        val action = event.action
        if (!action.name.contains("CLICK")) return
        castingCommandMap[player]?.let {
            val thisTimeAction = if (action.name.startsWith("LEFT_CLICK")) {
                player.playSound(player.getHeadLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.3f)
                "L"
            } else {
                player.playSound(player.getHeadLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 0.7f)
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
        player.playSound(player.getHeadLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
        player.sendActionBar(Component.text("Cast Mode canceled", NamedTextColor.RED))
    }

    fun getTool(): List<Material> {
        return targetTool
    }
    
    protected fun addTool(tool: Material): JobClassMaster {
        targetTool.add(tool)
        return this
    }

    fun isJobTool(tool: Material): Boolean {
        return targetTool.contains(tool)
    }

    protected fun registerSkill(skillCommand: SkillCommand, skill: ToggleSkillMaster) {
        skillMap[skillCommand] = skill
    }

    private fun activeSkill(player: Player) {
        val castingAction = castingCommandMap[player] ?: return
        try {
            val skillCommand = SkillCommand.valueOf(castingAction)
            val skill = skillMap[skillCommand]
            if (skill == null) {
                notRegisterActionNotice(player)
            } else {
                skill.toggleSkill(player, player.getJobClassLevel(this))
            }
        } catch (e: IllegalArgumentException) {
            notRegisterActionNotice(player)
        }
    }

    private fun notRegisterActionNotice(player: Player) {
        player.playSound(player.getHeadLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
        player.sendActionBar(Component.text("Not registered skill", NamedTextColor.RED))
    }

    open fun levelUpEvent(player: Player) {}
}
