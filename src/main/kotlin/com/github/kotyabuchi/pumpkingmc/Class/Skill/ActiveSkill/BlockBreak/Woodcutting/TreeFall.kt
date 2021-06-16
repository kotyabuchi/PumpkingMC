package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Woodcutting

import com.github.kotyabuchi.pumpkingmc.Class.Lifestyle.Woodcutting
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToggleSkillMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Utility.isLeave
import com.github.kotyabuchi.pumpkingmc.Utility.miningWithEvent
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object TreeFall: ToggleSkillMaster {
    override val skillName: String = "TreeFall"
    override val cost: Int = 0
    override val needLevel: Int = 0
    override var description: String = "伐採した際に木が落下してくる"

    @EventHandler(priority = EventPriority.HIGH)
    fun onBreakBlock(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        if (event.isCancelled) return

        val player = event.player
        if (!isEnabledSkill(player)) return
        val block = event.block
        val item = player.inventory.itemInMainHand
        val toolType = item.type

        if (!Woodcutting.isTargetBlock(block)) return
        if (!Woodcutting.isJobTool(toolType)) return

        val y = block.y

        val woodList: MutableList<Block> = mutableListOf()
        val leaveList: MutableList<Block> = mutableListOf()
        Woodcutting.searchWood(block, block, woodList, leaveList, mutableListOf())
        woodList.remove(block)
        woodList.addAll(leaveList)
        woodList.sortWith { o1, o2 -> o1.y - o2.y }
        woodList.forEach {
            object : BukkitRunnable() {
                override fun run() {
                    if (it.type.isLeave()) {
                        block.world.spawnFallingBlock(it.location.add(0.5, 0.0, 0.5), it.blockData)
                        it.type = Material.AIR
                    } else if (!it.getRelative(BlockFace.DOWN).type.isSolid) {
                        block.world.spawnFallingBlock(it.location.add(0.5, 0.0, 0.5), it.blockData)
                        it.type = Material.AIR
                    }
                }
            }.runTaskLater(instance, it.y - y.toLong())
        }
        block.miningWithEvent(player, item)
    }
}