package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Woodcutting

import com.github.kotyabuchi.pumpkingmc.Class.Lifestyle.Woodcutting
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.System.Player.getJobClassLevel
import com.github.kotyabuchi.pumpkingmc.Utility.isWood
import com.github.kotyabuchi.pumpkingmc.Utility.miningWithEvent
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

object TreeAssist: ActiveSkillMaster {
    override val skillName: String = "TreeAssist"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 50
    override val description: String = ""
    override val hasActiveTime: Boolean = true
    override val activePlayerLevelMap: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val lastUseTime: MutableMap<UUID, Long> = mutableMapOf()
    override fun calcActiveTime(level: Int): Int = 20 * 6

    @EventHandler
    fun onBlockMine(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        if (event.isCancelled) return

        val player = event.player

        if (!isEnabledSkill(player)) return
        val itemStack = player.inventory.itemInMainHand
        val block = event.block
        if (!Woodcutting.isTargetBlock(block)) return
        if (!Woodcutting.isJobTool(itemStack.type)) return
        restartActiveTime(player, player.getJobClassLevel(Woodcutting))

        val material = block.type
        if (!material.isWood()) return
        val y = block.y
        val woodList: MutableList<Block> = mutableListOf()
        val leaveList: MutableList<Block> = mutableListOf()

        Woodcutting.searchWood(block, block, woodList, leaveList, mutableListOf())
        leaveList.sortWith { o1, o2 -> o1.y - o2.y }

        woodList.forEach {
            it.miningWithEvent(player, itemStack, block)
        }
        leaveList.forEach {
            object : BukkitRunnable() {
                override fun run() {
                    block.world.spawnFallingBlock(it.location.add(0.5, 0.0, 0.5), it.blockData)
                    it.type = Material.AIR
                }
            }.runTaskLater(instance, it.y - y.toLong())
        }
    }
}