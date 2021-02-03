package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

object StrongShoot: ActiveSkillMaster {
    override val skillName: String = "StrongShoot"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 50
    override var description: String = "次の矢の速度が上昇する"
    override val hasActiveTime: Boolean = false
    override val activePlayers: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val coolTimePlayers: MutableList<UUID> = mutableListOf()

    override fun calcActiveTime(level: Int): Int = 0

    override fun enableAction(player: Player, level: Int) {
        player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
        player.sendActionMessage("&eStrongShoot ready x${1 + (level / 100.0).floor2Digits()}")
    }

    override fun disableAction(player: Player) {
    }

    @EventHandler
    fun onShot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val uuid = player.uniqueId
        val arrow = event.projectile as? Arrow ?: return
        if (!isEnabledSkill(uuid)) return
        val level = activePlayers[uuid] ?: 1
        val multiple = 1 + level / 100.0
        arrow.velocity = arrow.velocity.multiply(multiple)
    }
}