package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

object DoubleAttack: ActiveSkillMaster {
    override val skillName: String = "Double Attack"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 50
    override var description: String = "次の攻撃が2回攻撃になる"
    override val hasActiveTime: Boolean = false
    override val activePlayers: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val coolTimePlayers: MutableList<UUID> = mutableListOf()
    override fun calcActiveTime(level: Int): Int = 0

    override fun enableAction(player: Player, level: Int) {
        player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
        player.sendActionMessage("&e$skillName ready")
    }

    override fun disableAction(player: Player) {
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        val item = player.inventory.itemInMainHand
        if (!item.type.name.endsWith("_SWORD")) return

        if (isEnabledSkill(player.uniqueId)) {
            disableSkill(player)
            entity.damage(event.damage, player)
            entity.noDamageTicks = 0
        }
    }
}