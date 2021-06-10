package com.github.kotyabuchi.pumpkingmc.CustomEvent

import org.bukkit.Statistic
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import java.lang.Exception

class BlockMineEvent(block: Block, player: Player, val isMultiBreak: Boolean = false): BlockBreakEvent(block, player) {

    private var cancelled = false

    override fun setCancelled(cancel: Boolean) {
        super.setCancelled(cancel)
        if (cancel) {
            if (!cancelled) {
                cancelled = true
                try {
                    player.decrementStatistic(Statistic.MINE_BLOCK, block.type)
                } catch (e: Exception) {}
            }
        } else {
            if (cancelled) {
                cancelled = false
                try {
                    player.incrementStatistic(Statistic.MINE_BLOCK, block.type)
                } catch (e: Exception) {}
            }
        }
    }

    init {
        try {
            player.incrementStatistic(Statistic.MINE_BLOCK, block.type)
        } catch (e: Exception) {}
    }
}