package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockDropItemEvent

object Herbalism: BlockBreakJobClass(JobClassType.HERBALISM) {

    init {
        Material.values().forEach {
            if (it.name.endsWith("_HOE")) addTool(it)
        }
        addExpMap(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, exp = 1)
        addExpMap(Material.MELON, exp = 3)
        addExpMap(Material.PUMPKIN, exp = 4)
    }

    @EventHandler
    fun onMine(event: BlockMineEvent) {
        val blockData = event.block.blockData
        val item = event.player.inventory.itemInMainHand
        if (getTool().contains(item.type) && blockData is Ageable && blockData.age != blockData.maximumAge) event.isCancelled = true
    }

    @EventHandler
    fun onDrop(event: BlockDropItemEvent) {
        val block = event.block
        if (getTool().contains(event.player.inventory.itemInMainHand.type) && containsBrokenBlockSet(block)) {
            block.type = event.blockState.type
        }
    }
}
