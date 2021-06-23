package com.github.kotyabuchi.pumpkingmc.CustomItem

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent

abstract class ConsumableItem: ItemMaster() {

    abstract fun consume(player: Player, event: PlayerItemConsumeEvent? = null)
}