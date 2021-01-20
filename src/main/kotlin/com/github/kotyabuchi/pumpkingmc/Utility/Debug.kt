package com.github.kotyabuchi.pumpkingmc.Utility

import org.bukkit.Bukkit

fun debugChat(msg: String) {
    Bukkit.getPlayer("kabocchi")?.sendMessage(msg)
}