package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Mining

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToggleSkillMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Utility.aroundBlockFace
import com.github.kotyabuchi.pumpkingmc.Utility.isOre
import com.github.kotyabuchi.pumpkingmc.Utility.miningWithEvent
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import java.util.*

object MineAssist: ToggleSkillMaster {
    override val skillName: String = "MineAssist"
    override val cost: Int = 0
    override val needLevel: Int = 50
    override var description: String = "周囲の鉱石もまとめて採掘する"

    @EventHandler
    fun onBlockMine(event: BlockMineEvent) {
        if (event.isCancelled) return

        val player = event.player

        if (!isEnabledSkill(player)) return

        val block = event.block
        val itemStack = player.inventory.itemInMainHand
        if (!block.type.isOre()) return

        val ores = mutableListOf<Block>()
        searchOres(block, ores, mutableListOf())

        ores.forEach {
            if (block == it) {
                it.miningWithEvent(player, itemStack, block, event.isMultiBreak)
            } else {
                it.miningWithEvent(player, itemStack, block)
            }
        }
    }

    private fun searchOres(checkBlock: Block, oreList: MutableList<Block>, checkedList: MutableList<Block>) {
        if (checkedList.contains(checkBlock)) return
        checkedList.add(checkBlock)
        val checkMaterial = checkBlock.type
        if (!checkMaterial.isOre()) return
        oreList.add(checkBlock)
        if (checkedList.size > 496) return
        val upBlock = checkBlock.getRelative(BlockFace.UP)
        val downBlock = checkBlock.getRelative(BlockFace.DOWN)
        aroundBlockFace.forEach {
            searchOres(upBlock.getRelative(it), oreList, checkedList)
        }
        aroundBlockFace.forEach {
            searchOres(checkBlock.getRelative(it), oreList, checkedList)
        }
        aroundBlockFace.forEach {
            searchOres(downBlock.getRelative(it), oreList, checkedList)
        }
    }
}