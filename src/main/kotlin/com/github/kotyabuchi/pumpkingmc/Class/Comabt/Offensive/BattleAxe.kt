package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

object BattleAxe: JobClassMaster(JobClassType.BATTLEAXE) {

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        if (event.entity !is LivingEntity) return
        val item = player.inventory.itemInMainHand

        if (!item.type.name.endsWith("_AXE")) return
        val amount = event.finalDamage
        player.getStatus().addSkillExp(jobClassType, amount)
    }
}