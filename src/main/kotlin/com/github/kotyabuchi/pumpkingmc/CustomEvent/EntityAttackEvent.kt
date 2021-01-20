package com.github.kotyabuchi.pumpkingmc.CustomEvent

import com.github.kotyabuchi.pumpkingmc.Comabt.DamageInfo
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageEvent

class EntityAttackEvent(val damageInfo: DamageInfo, val baseEvent: EntityDamageEvent): Event(), Cancellable {

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlers
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}