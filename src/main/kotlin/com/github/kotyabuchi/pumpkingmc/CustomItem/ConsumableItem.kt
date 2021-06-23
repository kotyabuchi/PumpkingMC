package com.github.kotyabuchi.pumpkingmc.CustomItem

import org.bukkit.entity.Player

abstract class ConsumableItem: ItemMaster() {

    abstract fun consume(player: Player)
}