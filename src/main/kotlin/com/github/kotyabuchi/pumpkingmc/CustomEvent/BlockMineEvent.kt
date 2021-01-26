package com.github.kotyabuchi.pumpkingmc.CustomEvent

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

class BlockMineEvent(block: Block, player: Player, val isMultiBreak: Boolean = false): BlockBreakEvent(block, player) {
}