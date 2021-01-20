package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent

object Herbalism: com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass(JobClassType.HERBALISM) {

    init {
        Material.values().forEach {
            if (it.name.endsWith("_HOE")) addTool(it)
        }
    }
    
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val block = event.block
        val blockData = block.blockData
        if (blockData is Ageable) {
            if (blockData.age == blockData.maximumAge) {
                addBrokenBlockList(block)
            } else if (event.player.inventory.itemInMainHand.type.name.endsWith("_HOE")) {
                event.isCancelled = true
            }
        } else {
            val type = block.type
            if (type == Material.PUMPKIN || type == Material.MELON) addBrokenBlockList(block)
        }
    }

    @EventHandler
    fun onDrop(event: BlockDropItemEvent) {
        val block = event.block
        val player = event.player
        if (player.inventory.itemInMainHand.type.name.endsWith("_HOE") && containsBrokenBlockList(block)) block.type = event.blockState.type
    }
}
