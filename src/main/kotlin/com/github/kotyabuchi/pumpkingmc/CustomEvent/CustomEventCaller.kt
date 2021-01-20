package com.github.kotyabuchi.pumpkingmc.CustomEvent

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.event.Listener

class CustomEventCaller: Listener {

    private val pm = instance.server.pluginManager

//    @EventHandler
//    fun onPickupItem(event: PlayerAttemptPickupItemEvent) {
//        val player = event.player
//        val inv = player.inventory
//        val itemEntity = event.item
//        val item = itemEntity.itemStack
//        val getItemEvent = PlayerGetItemEvent(player, item)
//        val pickUppedAmount = item.amount
//        pm.callEvent(getItemEvent)
//        val eventItem = getItemEvent.item
//
//        if (eventItem == null) {
//            object : BukkitRunnable() {
//                override fun run() {
//                    inv.removeItem(item)
//                }
//            }.runTaskLater(instance, 0)
//        } else {
//            object : BukkitRunnable() {
//                val minusAmount = item.amount
//                override fun run() {
//                    item.amount = pickUppedAmount - minusAmount
//                    inv.removeItem(item)
//                }
//            }.runTaskLater(instance, 0)
//        }
//    }
}