package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityBreedEvent
import kotlin.math.floor
import kotlin.math.round
import kotlin.random.Random

object Breeding: JobClassMaster(JobClassType.BREEDING) {

    @EventHandler
    fun onSpawn(event: EntityBreedEvent) {
        val entity = event.entity

        var foundCount = 0
        entity.getNearbyEntities(30.0, 5.0, 30.0).forEach {
            if (it.type == entity.type) foundCount++
        }
        if (foundCount >= 30) {
            event.isCancelled = true
            val breeder = event.breeder as? Player ?: return
            breeder.sendMessage("&e繁殖可能数を超えています ($foundCount/30)")
        } else {
            val breeder = event.breeder as? Player ?: return
            val status = breeder.getStatus()
            val level = status.getJobClassStatus(jobClassType).getLevel()

            var amount = floor(level / 200.0).toInt() + 1
            if (Random.nextInt(200) <= level % 200) amount++

            for (i in 1 until amount) {
                entity.world.spawnEntity(entity.location, entity.type, CreatureSpawnEvent.SpawnReason.BREEDING)
            }



            event.experience = round(event.experience * (1 + level / 100.0) * amount).toInt()
            status.addSkillExp(jobClassType, 100.0 * amount)
        }
    }
}