package com.github.kotyabuchi.pumpkingmc.Utility

import org.bukkit.Location
import org.bukkit.entity.Player

fun Location.addSome(x: Double = .0, y: Double = .0, z: Double = .0): Location {
    return this.add(x, y, z)
}

fun Player.getHeadLocation(): Location {
    return this.location.addSome(y = this.eyeHeight)
}