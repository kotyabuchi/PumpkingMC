package com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery.ArcShot
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery.GravityShot
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery.StrongShot
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getJobClassLevel
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.floor2Digits
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import de.tr7zw.changeme.nbtapi.NBTEntity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType

object Archery: JobClassMaster("ARCHERY") {

    private val passBlocks: MutableSet<Material> = mutableSetOf()

    init {
        Material.values().forEach {
            if (!it.isSolid) passBlocks.add(it)
        }
        addTool(Material.BOW)
        addTool(Material.CROSSBOW)
        addAction(SkillCommand.RRR, 50, fun (player: Player) {
            StrongShot.enableSkill(player, player.getStatus().getJobClassStatus(this).getLevel())
        })
        addAction(SkillCommand.LLL, 200, fun (player: Player) {
            ArcShot.enableSkill(player, player.getStatus().getJobClassStatus(this).getLevel())
        })
        addAction(SkillCommand.LRL, 100, fun (player: Player) {
            GravityShot.enableSkill(player, player.getJobClassLevel(this))
        })
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val arrow = event.damager as? Arrow ?: return
        val player = arrow.shooter as? Player ?: return
        val entity = event.entity
        if (entity !is LivingEntity) return

        val nbte = NBTEntity(arrow)
        if (nbte.hasKey("Paper.Origin") && !arrow.persistentDataContainer.has(NamespacedKey(instance, "Disable_LongShotBonus"), PersistentDataType.BYTE)) {
            val doubleList = nbte.getDoubleList("Paper.Origin")
            val loc = Location(entity.world, doubleList[0], doubleList[1], doubleList[2])
            val distance = entity.location.distance(loc)
            if (distance >= 20) {
                val multiple = 1 + distance / 100.0
                event.damage *= multiple
                player.sendActionMessage("&9☆Long range bonus x${multiple.floor2Digits()}")
            }
        }
        player.getStatus().addSkillExp(this, event.finalDamage)
    }
}