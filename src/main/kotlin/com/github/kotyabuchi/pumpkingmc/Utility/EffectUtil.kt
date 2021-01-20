package com.github.kotyabuchi.pumpkingmc.Utility

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

fun drawLine(startLoc: Location, endLoc: Location) {
    val distance = startLoc.distance(endLoc)
    val dotAmount = round(distance * 5).toInt()
    val dotSpace = distance / dotAmount
    val lineVec = Vector(endLoc.x - startLoc.x, endLoc.y - startLoc.y, endLoc.z - startLoc.z).normalize().multiply(dotSpace)
    for (i in 0 until dotAmount) {
        startLoc.world?.spawnParticle(Particle.REDSTONE, startLoc.clone().add(lineVec.clone().multiply(i)), 1, .0, .0, .0, .0, Particle.DustOptions(Color.RED, 1f))
    }
}

fun drawCircle(radius: Double, pointAmount: Int = ceil(radius * 18).toInt(), startPoint: Double = 0.0): List<Pair<Double, Double>> {
    val result = mutableListOf<Pair<Double, Double>>()
    val width = 360.0 / pointAmount
    for (i in (0 until pointAmount)) {
        result.add(Pair(radius * cos(Math.toRadians(width * i + startPoint)), radius * -sin(Math.toRadians(width * i + startPoint))))
    }
    return result
}