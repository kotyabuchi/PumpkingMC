package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class RemoveLag {

    private var runnable: BukkitTask? = null

    fun start() {
        runnable = object : BukkitRunnable() {
            override fun run() {
                var countDown = 30
                object : BukkitRunnable() {
                    override fun run() {
                        if (countDown == 30) {
                            Bukkit.broadcastMessage("&4[System] &f30秒後にドロップアイテムを全削除します。必要なものは全て拾ってください。".colorS())
                        } else if (countDown == 0) {
                            var amount = 0
                            instance.server.worlds.forEach { world ->
                                val items = world.getEntitiesByClass(EntityType.DROPPED_ITEM.entityClass!!)
                                amount += items.size
                                items.forEach { item ->
                                    item.remove()
                                }
                            }
                            Bukkit.broadcastMessage("&4[System] &fドロップアイテムを全削除しました。 (${amount}個)".colorS())
                            cancel()
                        } else if (countDown <= 10 || countDown == 15) {
                            Bukkit.broadcastMessage("&4[System] ${countDown}...".colorS())
                        }
                        countDown--
                    }
                }.runTaskTimer(instance, 0, 20)
            }
        }.runTaskTimer(instance, 20 * 60 * 15, 20 * 60 * 15)
    }

    fun stop() {
        runnable?.cancel()
        runnable = null
    }
}