package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.superBreaker
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

object Excavation: com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass(JobClassType.EXCAVATION) {

    init {
        Material.values().forEach {
            if (it.name.endsWith("_SHOVEL")) addTool(it)
        }
        addExpMap(Material.CLAY, exp = 2)

        addAction(SkillCommand.RRR, 25, fun(player: Player) {
            superBreaker.enableSuperBreaker(player, jobClassType)
        })
    }
    
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        val playerStatus = player.getStatus()
        val block = event.block
        val type = block.type
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type)) return
        if (type == Material.DIRT || type == Material.SAND || type == Material.GRASS_BLOCK || type == Material.GRAVEL || type == Material.CLAY ||
                type == Material.FARMLAND || type == Material.GRASS_PATH || type == Material.COARSE_DIRT || type == Material.PODZOL ||
                type == Material.RED_SAND || type == Material.SOUL_SAND || type == Material.SOUL_SOIL) {
            addBrokenBlockList(block)

            val level = playerStatus.getJobClassStatus(jobClassType).getLevel()
            if (level >= 100 && Random.nextInt(1000) < min(1000, level) / 2) {
                val itemExpansion = ItemExpansion(item)
                val mendingAmount = floor(level / 100.0).toInt()
                itemExpansion.increaseDurability(mendingAmount)
                player.inventory.setItemInMainHand(itemExpansion.item)
                player.sendActionMessage("&dSoftTouch Mending +$mendingAmount")
            }
        }
    }
}
