package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.lang.Integer.max
import kotlin.random.Random

object BattleAxe: JobClassMaster("BATTLEAXE") {

    private fun getHeavyBlowChange(level: Int): Int = max(500, level)

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        val item = player.inventory.itemInMainHand

        if (!item.type.name.endsWith("_AXE")) return

        val status = player.getStatus()
        val jobClass = status.getJobClassStatus(this)
        val level = jobClass.getLevel()

        // Heavy Blow -
        val isHeavyBlowAttack = Random.nextInt(1000) <= getHeavyBlowChange(level) * if (player.fallDistance > 0) 1.5 else 1.0
        if (isHeavyBlowAttack) {
            entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20 * 3, 1, true, true))
            entity.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 20 * 6, 1, true, true))
            if (entity is Player) entity.playSound(entity.eyeLocation, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, .5f, 1.3f)
            player.playSound(entity.eyeLocation, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, .5f, 1.3f)
            player.sendActionMessage("&8Heavy Blow")
        }
        // - Heavy Blow

        val amount = event.finalDamage
        player.getStatus().addSkillExp(this, amount)
    }
}