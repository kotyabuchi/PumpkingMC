package com.github.kotyabuchi.pumpkingmc.Utility

import org.bukkit.entity.Player

fun Player.sendActionMessage(message: String) {
    val blank = "                                   "
    this.sendMessage(blank.substring(0 until (blank.length - (message.length / 2))) + message.colorS())
}