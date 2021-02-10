package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

object StrongShot: ActiveSkillMaster {
    override val skillName: String = "StrongShot"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 50
    override var description: String = "次の矢の速度が上昇する"
    override val hasActiveTime: Boolean = false
    override val activePlayerLevelMap: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val coolTimePlayers: MutableList<UUID> = mutableListOf()
    override fun calcActiveTime(level: Int): Int = 0

    private fun getVelocityMultiply(level: Int): Double = 1 + (level / 200.0)

    override fun enableAction(player: Player, level: Int) {
        player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
        player.sendActionMessage("&eStrongShoot ready x${getVelocityMultiply(level).floor2Digits()}")
    }

    override fun disableAction(player: Player) {
    }

    @EventHandler
    fun onShot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val uuid = player.uniqueId
        val arrow = event.projectile as? Arrow ?: return
        if (!isEnabledSkill(player)) return
        val level = activePlayerLevelMap[uuid] ?: 1
        val multiple = getVelocityMultiply(level)
        arrow.velocity = arrow.velocity.multiply(multiple)
        disableSkill(player)
    }
}