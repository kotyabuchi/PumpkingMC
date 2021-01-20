package com.github.kotyabuchi.pumpkingmc.System.Player

import com.github.kotyabuchi.pumpkingmc.Utility.loadPlayerStatus
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

private val playerStatusMap = mutableMapOf<UUID, PlayerStatus>()

fun Player.getStatus(): PlayerStatus {
    val uuid = this.uniqueId
    if (!playerStatusMap.containsKey(uuid)) playerStatusMap[uuid] = PlayerStatus(this)
    return playerStatusMap[uuid]!!
}

fun getAllPlayerStatus(): Collection<PlayerStatus> {
    val removeList = mutableListOf<UUID>()
    val result = mutableListOf<PlayerStatus>()
    val server = instance.server
    playerStatusMap.forEach { (t, u) ->
        if (server.getPlayer(t)?.isOnline == true) {
            result.add(u)
        } else {
            removeList.add(t)
        }
    }
    removeList.forEach {
        playerStatusMap.remove(it)
    }
    return result
}

class PlayerManager: Listener {

    init {
        val status = loadPlayerStatus(*instance.server.onlinePlayers.toTypedArray())

        status.forEach {
            playerStatusMap[it.player.uniqueId] = it
        }
    }

    @EventHandler
    fun onJoinServer(event: PlayerJoinEvent) {
        val player = event.player
        playerStatusMap[player.uniqueId] = loadPlayerStatus(player).first()
    }

    @EventHandler
    fun onQuitServer(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId
        playerStatusMap[uuid]?.save()
        playerStatusMap.remove(uuid)
    }
}