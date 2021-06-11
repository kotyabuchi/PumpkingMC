package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster

import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.SwordMaster
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

object DoubleAttack: ActiveSkillMaster {
    override val skillName: String = "DoubleAttack"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 50
    override var description: String = "次の攻撃が2回攻撃になる"
    override val hasActiveTime: Boolean = false
    override val activePlayerLevelMap: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val lastUseTime: MutableMap<UUID, Long> = mutableMapOf()
    override fun calcActiveTime(level: Int): Int = 0

    override fun enableAction(player: Player, level: Int) {
        player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
        player.sendActionMessage("&e$skillName ready")
    }

    override fun disableAction(player: Player) {
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        val player = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        if (!SwordMaster.isJobTool(player.inventory.itemInMainHand.type)) return
        if (!isEnabledSkill(player)) return

        disableSkill(player)
        val damageEvent = EntityDamageByEntityEvent(event.damager, event.entity, event.cause, event.damage)
        instance.callEvent(damageEvent)
        if (damageEvent.isCancelled) return
        entity.damage(event.damage, player)
        entity.noDamageTicks = 0
    }
}