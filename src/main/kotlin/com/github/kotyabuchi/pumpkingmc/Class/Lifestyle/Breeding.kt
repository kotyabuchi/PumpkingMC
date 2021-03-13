package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.consume
import com.github.kotyabuchi.pumpkingmc.Utility.findItemAmount
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

object Breeding: JobClassMaster("BREEDING") {

    @EventHandler
    fun onClickAnimals(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val entity = event.rightClicked as? Animals ?: return

        val level = player.getStatus().getJobClassStatus(this).getLevel()
        if (level < 50) return

        val item = player.inventory.itemInMainHand
        val isLoveMode = entity.isLoveMode
        if (isLoveMode) return

        object : BukkitRunnable() {
            override fun run() {
                if (!isLoveMode && entity.isLoveMode) {
                    val animals = mutableListOf<Animals>()
                    entity.getNearbyEntities(1 + level / 50.0, 2.0, 1 + level / 50.0).forEach {
                        if (it is Animals && entity.type == it.type && !it.isLoveMode) {
                            animals.add(it)
                        }
                    }
                    val amount = min(player.inventory.findItemAmount(item), animals.size)
                    player.inventory.consume(item, amount)
                    repeat(amount) {
                        animals[it].loveModeTicks = entity.loveModeTicks
                        animals[it].breedCause = player.uniqueId
                    }
                }
            }
        }.runTaskLater(instance, 0)
    }

    @EventHandler
    fun onSpawn(event: EntityBreedEvent) {
        val entity = event.entity
        val breeder = event.breeder as? Player ?: return

        var foundCount = 0
        entity.getNearbyEntities(30.0, 5.0, 30.0).forEach {
            if (it.type == entity.type) foundCount++
        }
        if (foundCount >= 30) {
            event.isCancelled = true
            breeder.sendMessage("&e繁殖可能数を超えています ($foundCount/30)")
        } else {
            val status = breeder.getStatus()
            val level = status.getJobClassStatus(this).getLevel()

            var amount = floor(level / 200.0).toInt() + 1
            if (Random.nextInt(200) <= level % 200) amount++

            for (i in 1 until amount) {
                entity.world.spawnEntity(entity.location, entity.type, CreatureSpawnEvent.SpawnReason.BREEDING)
            }

            event.experience = round(event.experience * (1 + level / 100.0) * amount).toInt()
            status.addSkillExp(this, 100.0 * amount)
        }
    }
}