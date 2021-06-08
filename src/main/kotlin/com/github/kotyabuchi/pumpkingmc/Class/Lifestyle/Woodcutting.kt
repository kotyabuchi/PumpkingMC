package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Woodcutting.TreeAssist
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Woodcutting.TreeFall
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.Enum.WoodType
import com.github.kotyabuchi.pumpkingmc.Utility.aroundBlockFace
import com.github.kotyabuchi.pumpkingmc.Utility.getWoodType
import com.github.kotyabuchi.pumpkingmc.Utility.isLeave
import com.github.kotyabuchi.pumpkingmc.Utility.isWood
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Leaves
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

object Woodcutting: BlockBreakJobClass("WOOD_CUTTING") {

    init {
        Material.values().forEach {
            if (it.name.endsWith("_AXE")) addTool(it)
            if (it.isWood()) addExpMap(it, exp = 1)
        }

        registerSkill(SkillCommand.LLL, TreeFall)
        registerSkill(SkillCommand.LRL, TreeAssist)
    }

    @EventHandler
    fun onLand(event: EntityChangeBlockEvent) {
        val entity = event.entity as? FallingBlock ?: return
        val material = entity.blockData.material
        if (material.isLeave()) {
            dropFromLeave(entity)
            event.isCancelled = true
            entity.remove()
        }
    }

    @EventHandler
    fun onDropItemFromEntity(event: EntityDropItemEvent) {
        val entity = event.entity as? FallingBlock ?: return
        val material = entity.blockData.material
        if (material.isLeave()) {
            dropFromLeave(entity)
            event.isCancelled = true
            entity.remove()
        }
    }

    private fun dropFromLeave(entity: FallingBlock) {
        val material = entity.blockData.material
        if (Random.nextInt(100) < 5) {
            val item = entity.world.dropItem(entity.location, ItemStack(Material.valueOf(material.name.replace("LEAVES", "SAPLING"))))
            instance.server.pluginManager.callEvent(EntityDropItemEvent(entity, item))
        }
        if (Random.nextInt(1000) < 15) {
            val item = entity.world.dropItem(entity.location, ItemStack(Material.STICK, Random.nextInt(2) + 1))
            instance.server.pluginManager.callEvent(EntityDropItemEvent(entity, item))
        }
        if (material.name.contains("OAK") && Random.nextInt(1000) < 5) {
            val item = entity.world.dropItem(entity.location, ItemStack(Material.APPLE, 1))
            instance.server.pluginManager.callEvent(EntityDropItemEvent(entity, item))
        }
    }

    fun searchWood(mainBlock: Block, checkBlock: Block, woodList: MutableList<Block>, leaveList: MutableList<Block>, checkedList: MutableList<Block>) {
        if (checkedList.contains(checkBlock)) return
        checkedList.add(checkBlock)
        if (mainBlock.location.y > checkBlock.location.y) return
        val mainMaterial = mainBlock.type
        val checkMaterial = checkBlock.type
        if (!checkMaterial.isWood()) {
            if (checkMaterial.isLeave()) searchLeave(mainBlock, checkBlock, checkMaterial.getWoodType()!!, leaveList, 0)
            return
        }
        if (mainMaterial.getWoodType() != checkMaterial.getWoodType()) return
        woodList.add(checkBlock)
        if (checkedList.size > 496) return
        val upBlock = checkBlock.getRelative(BlockFace.UP)
        val downBlock = checkBlock.getRelative(BlockFace.DOWN)
        aroundBlockFace.forEach {
            searchWood(mainBlock, upBlock.getRelative(it), woodList, leaveList, checkedList)
        }
        aroundBlockFace.forEach {
            searchWood(mainBlock, checkBlock.getRelative(it), woodList, leaveList, checkedList)
        }
        aroundBlockFace.forEach {
            searchWood(mainBlock, downBlock.getRelative(it), woodList, leaveList, checkedList)
        }
    }

    fun searchLeave(beforeBlock: Block, checkBlock: Block, woodType: WoodType, leaveList: MutableList<Block>, _count: Int) {
        var count = _count
        if (leaveList.contains(checkBlock)) return
        val checkMaterial = checkBlock.type
        if (!checkMaterial.isLeave()) return
        val leaves = checkBlock.blockData as Leaves
        if (checkMaterial.getWoodType() != woodType) return
        if (leaves.isPersistent) return
        if (beforeBlock.type.isLeave() && (beforeBlock.blockData as Leaves).distance >= leaves.distance) return
        leaveList.add(checkBlock)
        count++
        if (count > 496) return
        val upBlock = checkBlock.getRelative(BlockFace.UP)
        val downBlock = checkBlock.getRelative(BlockFace.DOWN)
        aroundBlockFace.forEach {
            searchLeave(checkBlock, upBlock.getRelative(it), woodType, leaveList, count)
        }
        aroundBlockFace.forEach {
            searchLeave(checkBlock, checkBlock.getRelative(it), woodType, leaveList, count)
        }
        aroundBlockFace.forEach {
            searchLeave(checkBlock, downBlock.getRelative(it), woodType, leaveList, count)
        }
    }
}
