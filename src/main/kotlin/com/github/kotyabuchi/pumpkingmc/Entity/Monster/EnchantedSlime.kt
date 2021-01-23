package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Slime

class EnchantedSlime: MobExpansionMaster(EntityType.SLIME) {

    init {
//        addSpawnAction { slime ->
//            slime as Slime
//            object : BukkitRunnable() {
//                override fun run() {
//                    if (slime.isDead) {
//                        cancel()
//                    } else if (slime.canWander() && slime.target == null) {
//                        val nearSlimes = slime.location.getNearbyEntitiesByType(Slime::class.java, slime.size.toDouble() * 2)
//                        var increaseSize = 0
//                        for (nearSlime in nearSlimes) {
//                            if (slime.size == nearSlime.size) {
//                                increaseSize++
//                                nearSlime.remove()
//                            }
//                        }
//                        if (increaseSize > 0) {
//                            slime.size += increaseSize
//                            slime.world.playSound(slime.location, Sound.ENTITY_SLIME_HURT, 1f, .5f)
//                            slime.world.playSound(slime.location, Sound.ENTITY_PUFFER_FISH_BLOW_UP, .5f, .5f)
//                            slime.world.playSound(slime.location, Sound.BLOCK_HONEY_BLOCK_STEP, .3f, .5f)
//                        }
//                    }
//                }
//            }.runTaskTimer(instance, 20, 20)
//        }
        addInFightAction((0 until 2), fun(slime: Mob) {
            if (slime is Slime) {
                if (slime.passengers.isEmpty() && slime.size <= 5) {
                    slime.size++
                    slime.world.playSound(slime.location, Sound.ENTITY_SLIME_HURT, 1f, .5f)
                    slime.world.playSound(slime.location, Sound.ENTITY_PUFFER_FISH_BLOW_UP, .5f, .5f)
                    slime.world.playSound(slime.location, Sound.BLOCK_HONEY_BLOCK_STEP, .3f, .5f)
                }
            }
        })
//        addInFightAction((0 until 10), fun(slime: Mob) {
//            slime.target?.let { target->
//                val baseLoc = slime.location.clone()
//                val vec = target.location.clone().add(.0, 5.0, .0).toVector().subtract(slime.location.toVector()).normalize().multiply(0.2)
//                for (i in 0 until 100) {
//                    baseLoc.world?.spawnParticle(Particle.REDSTONE, baseLoc.add(vec), 1, .0, .0, .0, .0, Particle.DustOptions(Color.RED, 1f))
//                    vec.subtract(Vector(.0, .002, .0))
//                }
//            }
//        })
    }

//    @EventHandler
//    fun onWander(event: SlimePathfindEvent) {
//        val mainSlime = event.entity
//
//        if (!mainSlime.canWander()) return
//
//        for (nearSlime in mainSlime.location.getNearbyEntitiesByType(Slime::class.java, 16.0, 4.0)) {
//            if (mainSlime.size == nearSlime.size && nearSlime.canWander()) {
//                val result = mainSlime.pathfinder.findPath(nearSlime) ?: continue
//                val finalPoint = result.finalPoint ?: continue
//                if (finalPoint.distance(nearSlime.location) <= 1) {
//                    mainSlime.pathfinder.moveTo(result)
//                    nearSlime.pathfinder.moveTo(mainSlime)
//                }
//            }
//        }
//    }
}