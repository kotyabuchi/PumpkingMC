package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster.BlinkStrike
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster.DoubleAttack
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

object SwordMaster: JobClassMaster("SWORDMASTER") {

    init {
        Material.values().forEach {
            if (it.name.endsWith("_SWORD")) addTool(it)
        }

        registerSkill(SkillCommand.RRR, DoubleAttack)
        registerSkill(SkillCommand.LLL, BlinkStrike)
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        val player = event.damager as? Player ?: return
        if (event.entity !is LivingEntity) return
        val item = player.inventory.itemInMainHand

        if (!item.type.name.endsWith("_SWORD")) return
        val amount = event.finalDamage
        player.getStatus().addSkillExp(this, amount)
    }
}