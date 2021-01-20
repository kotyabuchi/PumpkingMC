package com.github.kotyabuchi.pumpkingmc.Utility

import org.bukkit.Location
import org.bukkit.entity.Entity

fun Entity.jump(loc: Location) {
    val entityLoc = this.location
    val targetLoc = loc.add(.0, entityLoc.distance(loc) / 40.0, .0).toVector()
    val vel = targetLoc.clone().subtract(entityLoc.toVector()).multiply(0.3)
    if (vel.x > 4.0) {
        vel.x = 4.0
    } else if (vel.x < -4.0) {
        vel.x = -4.0
    }
    if (vel.y > 4.0) {
        vel.y = 4.0
    } else if (vel.y < -4.0) {
        vel.y = -4.0
    }
    if (vel.z > 4.0) {
        vel.z = 4.0
    } else if (vel.z < -4.0) {
        vel.z = -4.0
    }
    this.velocity = vel
}