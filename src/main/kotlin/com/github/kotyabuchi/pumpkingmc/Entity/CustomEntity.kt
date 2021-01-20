package com.github.kotyabuchi.pumpkingmc.Entity

import com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity.HollowKnightAxe
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.CustomEntity.SlimeSkeleton
import org.bukkit.entity.EntityType
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import kotlin.random.Random

class CustomEntity: Listener {

    @EventHandler
    fun onSpawn(event: EntitySpawnEvent) {
        val entity = event.entity

        when (entity.type) {
            EntityType.ZOMBIE -> {
                if (Random.nextInt(100) < 15) {
                    HollowKnightAxe.spawn(entity.location)
                }
            }
            EntityType.SKELETON, EntityType.STRAY -> {
                if (Random.nextInt(100) < 10) {
                    SlimeSkeleton.ride(entity as Skeleton)
                }
            }
            EntityType.WITHER_SKELETON -> {
                if (Random.nextInt(100) < 10) {
                    SlimeSkeleton.ride(entity as Skeleton, EntityType.MAGMA_CUBE)
                }
            }
            else -> {

            }
        }
    }
}