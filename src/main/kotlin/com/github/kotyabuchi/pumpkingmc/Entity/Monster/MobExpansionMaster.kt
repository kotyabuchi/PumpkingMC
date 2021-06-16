package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

open class MobExpansionMaster(private vararg val types: EntityType): Listener {

    private val lastAttackTime = mutableMapOf<LivingEntity, Long>()
    private val actionRunnable = mutableMapOf<LivingEntity, BukkitTask>()
    private val spawnAction = mutableListOf<Pair<IntRange, (entity: Mob) -> Unit>>()
    private val startFightAction = mutableListOf<Pair<IntRange, (entity: Mob, target: LivingEntity) -> Unit>>()
    private val inFightAction = mutableListOf<Pair<IntRange, (entity: Mob, target: LivingEntity) -> Unit>>()
    private val getDamageAction = mutableListOf<Pair<IntRange, (entity: Mob, target: LivingEntity) -> Unit>>()
    private val endFightAction = mutableListOf<(entity: Mob, target: LivingEntity?) -> Unit>()

    @EventHandler
    fun onSpawn(event: EntitySpawnEvent) {
        val entity = event.entity as? Mob ?: return
        if (!types.contains(entity.type)) return
        spawnAction.forEach {
            if (it.first.contains(Random.nextInt(100))) {
                it.second.invoke(entity)
            }
        }
    }

    @EventHandler
    fun onTarget(event: EntityTargetLivingEntityEvent) {
        val entity = event.entity as? Mob ?: return
        if (!types.contains(entity.type)) return
        val target = entity.target ?: return
        startFightAction.forEach {
            if (it.first.contains(Random.nextInt(100))) {
                it.second.invoke(entity, target)
            }
        }
        if (inFightAction.isNotEmpty()) startRunnable(entity)
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val damager = event.damager as? LivingEntity ?: return
        if (types.contains(damager.type)) {
            lastAttackTime[damager] = System.currentTimeMillis()
        } else if (types.contains(entity.type)) {
            entity as Mob
            if (inFightAction.isNotEmpty()) startRunnable(entity)
            getDamageAction.forEach {
                if (it.first.contains(Random.nextInt(100))) it.second.invoke(entity, damager)
            }
        }
    }

    fun addSpawnAction(probability: IntRange = (0 until 100), action: (entity: Mob) -> Unit) {
        spawnAction.add(probability to action)
    }

    fun addStartFightAction(probability: IntRange = (0 until 100), action: (entity: Mob, target: LivingEntity) -> Unit) {
        startFightAction.add(probability to action)
    }

    fun addInFightAction(probability: IntRange = (0 until 100), action: (entity: Mob, target: LivingEntity) -> Unit) {
        inFightAction.add(probability to action)
    }

    fun addGetDamageAction(probability: IntRange = (0 until 100), action: (entity: Mob, target: LivingEntity) -> Unit) {
        getDamageAction.add(probability to action)
    }

    fun addEndFightAction(action: (entity: Mob, target: LivingEntity?) -> Unit) {
        endFightAction.add(action)
    }

    fun setLastAttackTime(entity: Mob, time: Long) {
        lastAttackTime[entity] = time
    }

    fun getLastAttackTime(entity: Mob): Long {
        return lastAttackTime[entity] ?: 0
    }

    fun startRunnable(entity: Mob) {
        if (actionRunnable.containsKey(entity)) return
        actionRunnable[entity] = object : BukkitRunnable() {
            override fun run() {
                val target = entity.target
                if (entity.isDead || target == null) {
                    endFightAction.forEach {
                        it.invoke(entity, target)
                    }
                    actionRunnable.remove(entity)
                    cancel()
                } else {
                    inFightAction.forEach {
                        if (it.first.contains(Random.nextInt(100))) {
                            it.second.invoke(entity, target)
                        }
                    }
                }
            }
        }.runTaskTimer(instance, 0, 10)
    }
}