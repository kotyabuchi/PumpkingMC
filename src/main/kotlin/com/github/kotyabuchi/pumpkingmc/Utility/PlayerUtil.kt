package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

fun Player.sendActionMessage(message: String) {
    val blank = "                                   "
    this.sendMessage(blank.substring(0 until (blank.length - (message.length / 2))) + message.colorS())
}

fun Player.hasTag(tagName: String, dataType: PersistentDataType<out Any, out Any> = PersistentDataType.BYTE): Boolean {
    val pdc = this.persistentDataContainer
    return pdc.has(NamespacedKey(instance, tagName), dataType)
}

fun Player.toggleTag(tagName: String): Boolean {
    val pdc = this.persistentDataContainer
    return if (pdc.has(NamespacedKey(instance, tagName), PersistentDataType.BYTE)) {
        pdc.remove(NamespacedKey(instance, tagName))
        false
    } else {
        pdc.set(NamespacedKey(instance, tagName), PersistentDataType.BYTE, 1)
        true
    }
}