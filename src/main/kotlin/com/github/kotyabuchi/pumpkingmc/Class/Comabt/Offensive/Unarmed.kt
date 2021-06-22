package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

object Unarmed: JobClassMaster("UNARMED") {

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        val player = event.damager as? Player ?: return
        val playerStatus = player.getStatus()
        if (event.entity !is LivingEntity) return
        val item = player.inventory.itemInMainHand

        if (!item.type.isAir) return
        val isCritical = event.damage >= 1.5
        event.damage++
        event.damage += playerStatus.getJobClassStatus(this).getLevel() / 100.0
        if (isCritical) event.damage = event.damage * 1.5
        val amount = event.finalDamage
        playerStatus.addSkillExp(this, amount)
    }
}