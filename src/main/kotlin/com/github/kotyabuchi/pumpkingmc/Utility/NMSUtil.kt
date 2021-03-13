package com.github.kotyabuchi.pumpkingmc.Utility

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun getServerVersion(): String {
    return Bukkit.getServer().javaClass.`package`.name.substring(23)
}

//fun getNMSClass(name: String): Class<Any?> {
//}

fun getNMSClass(name: String): Class<*>? {
    return try {
        Class.forName("net.minecraft.server.${getServerVersion()}.$name")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getNMSBlock(material: Material): Any? {
    return try {
        val method = Class.forName("org.bukkit.craftbukkit.${getServerVersion()}.util.CraftMagicNumbers").getMethod("getBlock", Material::class.java)
        method.isAccessible = true
        method.invoke(material, material)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getCraftPlayer(player: Player): Any? {
    return try {
        Class.forName("org.bukkit.craftbukkit.${getServerVersion()}.entity.CraftPlayer").getMethod("getHandle").invoke(player)
    } catch (e: Exception) {
        null
    }
}

fun getPing(player: Player): Int? {
    return try {
        val craftPlayer = getCraftPlayer(player) ?: return null
        craftPlayer.javaClass.getField("ping").get(craftPlayer) as? Int
    } catch (e: Exception) {
        null
    }
}