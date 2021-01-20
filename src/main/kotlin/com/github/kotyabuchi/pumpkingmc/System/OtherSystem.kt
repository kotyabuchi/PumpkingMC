package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Utility.hasDurability
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.*
import kotlin.math.min
import kotlin.math.round

class OtherSystem: Listener {
    
    private val sleepingPlayer = mutableListOf<Player>()
    
    @EventHandler
    fun onEnterBed(event: PlayerBedEnterEvent) {
        val player = event.player
        if (event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK) return
        sleepingPlayer.add(player)
        player.world.players.forEach {
            it.sendMessage("[System] ${player.displayName}さんが寝ようとしています。 ${sleepingPlayer.size}/" + player.world.players.size)
        }
        if (sleepingPlayer.size >= round(player.world.players.size / 2.0)) {
            sleepingPlayer.forEach {
                val amount = (it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0) - it.health
                it.health += amount
                it.foodLevel -= round(min(amount / 2, 10.0)).toInt()
                instance.server.pluginManager.callEvent(EntityRegainHealthEvent(player, amount, EntityRegainHealthEvent.RegainReason.CUSTOM))
            }
            val world = player.world
            world.time = 0
            world.setStorm(false)
            world.isThundering = false
        }
    }
    
    @EventHandler
    fun onLeaveBed(event: PlayerBedLeaveEvent) {
        val player = event.player
        sleepingPlayer.remove(player)
        player.world.players.forEach {
            it.sendMessage("[System] ${player.displayName}さんがベッドから起きました。 ${sleepingPlayer.size}/${player.world.players.size}")
        }
    }

    @EventHandler
    fun onQuitServer(event: PlayerQuitEvent) {
        val player = event.player
        if (sleepingPlayer.contains(player)) {
            sleepingPlayer.remove(player)
            player.world.players.forEach {
                it.sendMessage("[System] ${player.displayName}さんがベッドから起きました。 ${sleepingPlayer.size}/${player.world.players.size}")
            }
        } else if (sleepingPlayer.size != 0) {
            player.world.players.forEach {
                it.sendMessage("[System] 現在寝ているのは${sleepingPlayer.size}/${player.world.players.size}人です。")
            }
        }
    }

    @EventHandler
    fun onPickupExp(event: PlayerExpChangeEvent) {
        val player = event.player
        var amount = event.amount * 2
        val orb = player.world.spawnEntity(player.location, EntityType.EXPERIENCE_ORB) as ExperienceOrb
        orb.experience = 0
        for (item in player.inventory.contents) {
            if (amount <= 0) break
            if (item != null && item.type.hasDurability() && item.itemMeta?.hasEnchant(Enchantment.MENDING) == true) {
                val expansion = ItemExpansion(item)
                if (expansion.isDurabilityDamaged()) {
                    val mendAmount = min(amount, expansion.getMaxDurability() - expansion.getDurability())
                    amount -= mendAmount
                    instance.server.pluginManager.callEvent(PlayerItemMendEvent(player, item, orb, mendAmount))
                }
            }
        }
        event.amount = amount
    }
}
