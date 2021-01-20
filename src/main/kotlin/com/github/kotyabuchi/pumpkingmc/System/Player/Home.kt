package com.github.kotyabuchi.pumpkingmc.System.Player

import org.bukkit.Material
import org.bukkit.World

data class Home(val homeId: Int?, val name: String, val world: World, val x: Double, val y: Double, val z: Double, val yaw: Float, var icon: Material) {

    fun changeIcon(icon: Material) {
        this.icon = icon
    }
}