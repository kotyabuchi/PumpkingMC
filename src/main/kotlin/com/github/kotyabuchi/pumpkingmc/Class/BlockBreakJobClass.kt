package com.github.kotyabuchi.pumpkingmc.Class

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockDropItemEvent
import kotlin.math.floor
import kotlin.random.Random

open class BlockBreakJobClass(jobClassType: JobClassType): JobClassMaster(jobClassType) {

    private val expMap = mutableMapOf<Material, Int>()
    private val brokenBlockList = mutableListOf<Block>()

    fun addExpMap(vararg materials: Material, exp: Int) {
        materials.forEach {
            expMap[it] = exp
        }
    }

    fun isTargetBlock(block: Block): Boolean {
        return expMap.containsKey(block.type)
    }

    fun addBrokenBlockList(block: Block) {
        brokenBlockList.add(block)
    }

    fun containsBrokenBlockList(block: Block): Boolean {
        return brokenBlockList.contains(block)
    }

    open fun afterDropAction(event: BlockDropItemEvent) {}

    @EventHandler(priority = EventPriority.HIGH)
    fun onDropItemFromBlock(event: BlockDropItemEvent) {
        val block = event.block
        val blockState = event.blockState
        val player = event.player
        val playerStatus = player.getStatus()

        if (brokenBlockList.contains(block)) {
            var exp = 0.0
            val doubleDropChance = playerStatus.getJobClassStatus(jobClassType).getLevel() / 3
            var dropAmount = 1 + floor(doubleDropChance / 100.0).toInt()
            if (Random.nextInt(100) < doubleDropChance % 100) {
                dropAmount++
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.0f)
            }
            event.items.forEach {
                val item = it.itemStack
                val itemExp = expMap[blockState.type] ?: 1
                item.amount *= dropAmount
                exp += (itemExp * item.amount)
            }
            if (SuperBreaker.isSuperBreaking(player)) exp *= 1.5

            playerStatus.addSkillExp(jobClassType, exp)
            afterDropAction(event)
        }
        brokenBlockList.remove(block)
    }
}