package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster.BlinkStrike
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster.DoubleAttack
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getJobClassLevel
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

        addAction(SkillCommand.RRR, 50, fun(player: Player) {
            DoubleAttack.enableSkill(player, player.getJobClassLevel(this))
        })
        addAction(SkillCommand.LLL, 100, fun(player: Player) {
            BlinkStrike.enableSkill(player, player.getJobClassLevel(this))
        })
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        if (event.entity !is LivingEntity) return
        val item = player.inventory.itemInMainHand

        if (!item.type.name.endsWith("_SWORD")) return
        val amount = event.finalDamage
        player.getStatus().addSkillExp(this, amount)
    }
}