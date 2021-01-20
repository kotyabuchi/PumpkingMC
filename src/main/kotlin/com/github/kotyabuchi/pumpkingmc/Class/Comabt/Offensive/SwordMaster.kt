package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

object SwordMaster: JobClassMaster(JobClassType.SWORDMASTER) {

    private val doubleAttackList = mutableListOf<Player>()

    init {
        Material.values().forEach {
            if (it.name.endsWith("_SWORD")) addTool(it)
        }
        addAction(SkillCommand.RRR, 100, fun(player: Player) {
            if (!doubleAttackList.contains(player)) doubleAttackList.add(player)
            player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
            player.sendActionMessage("&eDoubleAttack ready")
        })
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        if (event.entity !is LivingEntity) return
        val item = player.inventory.itemInMainHand

        if (!item.type.name.endsWith("_SWORD")) return
        if (doubleAttackList.contains(player)) doubleAttack(player, event)
        val amount = event.finalDamage
        player.getStatus().addSkillExp(jobClassType, amount)
    }

    private fun doubleAttack(player: Player, event: EntityDamageByEntityEvent) {
        doubleAttackList.remove(player)
        val entity = event.entity as? LivingEntity ?: return
        entity.damage(event.damage, player)
        entity.noDamageTicks = 0
    }
}