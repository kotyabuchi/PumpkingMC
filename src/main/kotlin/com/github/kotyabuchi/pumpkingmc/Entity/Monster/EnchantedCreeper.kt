package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.Utility.isGrass
import com.github.kotyabuchi.pumpkingmc.Utility.isLiquid
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.Container
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.round
import kotlin.random.Random

class EnchantedCreeper: MobExpansionMaster(EntityType.CREEPER) {

    private val jumpBlockBackup = mutableMapOf<Entity, BlockState>()

    init {
        addSpawnAction { creeper ->
            creeper.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = (creeper.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.value ?: 0.22) + (Random.nextInt(10) / 100.0)
        }

        addSpawnAction(0 until 10) { creeper ->
            creeper as Creeper
            creeper.isPowered = true
        }

        addStartFightAction { creeper ->
            creeper.target?.let { target ->
                creeper.getNearbyEntities(15.0, 5.0, 15.0).forEach {
                    if (it is Silverfish && it.target == null) {
                        it.target = target
                        instance.server.pluginManager.callEvent(EntityTargetLivingEntityEvent(it, target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER))
                    }
                    if (it is Creeper && it.target == null) {
                        it.target = target
                        instance.server.pluginManager.callEvent(EntityTargetLivingEntityEvent(it, target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER))
                    }
                }
            }
        }

        addStartFightAction { creeper ->
            object : BukkitRunnable() {
                override fun run() {
                    val target = creeper.target
                    if (creeper.isDead || target == null) {
                        cancel()
                    } else if (!creeper.persistentDataContainer.has(NamespacedKey(instance, "Jumped"), PersistentDataType.BYTE)) {
                        val creeperLoc = creeper.location
                        val targetLoc = target.location
                        if (targetLoc.block.y >= creeperLoc.block.y) {
                            val lookingDire = creeper.eyeLocation.direction.normalize()
                            lookingDire.y = 0.0
                            val baseBlockLoc = creeperLoc.block.location.add(.5, .0, .5)
                            val targetBlock = baseBlockLoc.clone().add(.0, -0.7, .0).add(lookingDire.clone().multiply(0.75)).block
                            val targetType = targetBlock.type
                            var canJump = false
                            var jumpBlock: Block? = null
                            if (targetType.isAir || targetType.isLiquid() || targetType.isGrass()) {
                                var checkTopLoc = baseBlockLoc.clone().add(lookingDire).add(.0, 2.5, .0)
                                var checkUnderLoc = baseBlockLoc.clone().add(lookingDire).add(.0, -.7, .0)
                                var airCount = 0
                                for (i in (0 until 4)) {
                                    val checkType = checkTopLoc.block.type
                                    if (checkType.isAir || checkType.isGrass()) {
                                        canJump = true
                                    } else {
                                        canJump = false
                                        break
                                    }
                                    checkTopLoc = checkTopLoc.add(lookingDire)
                                }
                                if (canJump) {
                                    for (i in (0 until 4)) {
                                        val checkType = checkUnderLoc.block.type
                                        if (checkType.isAir || checkType.isLiquid() || checkType.isGrass()) {
                                            airCount++
                                            canJump = false
                                        } else if (airCount >= 2) {
                                            val check1 = checkUnderLoc.block.getRelative(BlockFace.UP)
                                            val check2 = check1.getRelative(BlockFace.UP)
                                            if ((check1.type.isAir || check1.type.isLiquid() || check1.type.isGrass()) && (check2.type.isAir || check2.type.isLiquid() || check2.type.isGrass())) {
                                                canJump = true
                                                jumpBlock = checkUnderLoc.block
                                                break
                                            } else {
                                                canJump = false
                                                break
                                            }
                                        } else {
                                            canJump = false
                                            break
                                        }
                                        checkUnderLoc = checkUnderLoc.add(lookingDire)
                                    }
                                }
                                if (canJump) {
                                    val jumpLoc = jumpBlock!!.getRelative(BlockFace.UP).location.add(.5, 1.8, .5)
                                    creeper.velocity = jumpLoc.toVector().subtract(creeperLoc.toVector()).multiply(0.2)
                                    creeper.persistentDataContainer.set(NamespacedKey(instance, "Jumped"), PersistentDataType.BYTE, 1)
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            creeper.persistentDataContainer.remove(NamespacedKey(instance, "Jumped"))
                                        }
                                    }.runTaskLater(instance, 10)
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(instance, 0, 2)
        }

        addInFightAction(0 until 10) { creeper ->
            creeper as Creeper

            if (creeper.isIgnited) return@addInFightAction
            creeper.ignite()
            creeper.world.playSound(creeper.location, Sound.ENTITY_GHAST_SHOOT, .5f, .5f)
            object : BukkitRunnable() {
                override fun run() {
                    if (creeper.isDead) return
                    creeper.isIgnited = false
                    var radius = creeper.explosionRadius * 2
                    if (creeper.isPowered) radius = round(radius * 1.5).toInt()
                    val centerBlock = creeper.location.block.getRelative(BlockFace.DOWN)
                    val centerLoc = centerBlock.location.add(.5, .0, .5)

                    val jumpBlocks = mutableListOf<BlockState>()

                    for (x in (0..(radius * 2))) {
                        for (z in (0 ..(radius * 2))) {
                            if (centerLoc.distance(centerLoc.clone().add(x.toDouble() - radius, .0, z.toDouble() - radius)) > radius) continue
                            for (y in (8 downTo 0)) {
                                val checkBlock = centerLoc.clone().add(x.toDouble() - radius, y.toDouble() - 4, z.toDouble() - radius).block
                                if (checkBlock.type.isAir) {
                                    var downBlock = checkBlock.getRelative(BlockFace.DOWN)
                                    var downType = downBlock.type
                                    if (!downType.isAir && !downType.isLiquid()) {
                                        if (downType.isInteractable && downBlock.state is Container) continue
                                        val buffer = mutableListOf<BlockState>()
                                        var moreDownCount = y
                                        var success = true
                                        while (moreDownCount > 0 && !downType.isOccluding) {
                                            if (downType.isAir || downType.isLiquid()) {
                                                success = false
                                                break
                                            }
                                            moreDownCount--
                                            buffer.add(downBlock.state)
                                            downBlock = downBlock.getRelative(BlockFace.DOWN)
                                            downType = downBlock.type
                                        }
                                        if (success) {
                                            jumpBlocks.addAll(buffer)
                                            jumpBlocks.add(downBlock.state)
                                        }
                                        break
                                    }
                                }
                            }
                        }
                    }

                    jumpBlocks.forEach { blockState ->
                        val blockCenterLoc = blockState.location.add(.5, .0, .5)
                        object : BukkitRunnable() {
                            override fun run() {
                                val fallingBlock = blockState.world.spawnFallingBlock(blockCenterLoc, blockState.blockData)
                                fallingBlock.persistentDataContainer.set(NamespacedKey(instance, "SHAKE_EARTH"), PersistentDataType.BYTE, 1)
                                fallingBlock.velocity = Vector(.0, 1.0, .0)
                                jumpBlockBackup[fallingBlock] = blockState
                                blockState.world.spawnParticle(Particle.BLOCK_CRACK, blockState.location.add(.5, 1.0, .5), 20, .5, .3, .5, 2.0, blockState.type.createBlockData())
                                blockState.world.spawnParticle(Particle.EXPLOSION_LARGE, blockState.location.add(.5, .5, .5), 1)
                                blockState.block.type = Material.AIR
                                blockState.world.playSound(blockState.location, Sound.ENTITY_GENERIC_EXPLODE, .5f, 1f)
                                blockCenterLoc.getNearbyEntities(1.0, 3.5, 1.0).forEach {
                                    if (it !is FallingBlock && it !is Creeper && it is LivingEntity) {
                                        it.damage(4.0, creeper)
                                        it.velocity = it.velocity.add(Vector(.0, .5, .0))
                                    }
                                }
                            }
                        }.runTaskLater(instance, round(centerLoc.distance(blockCenterLoc)).toLong() * (30 / radius))
                    }
                }
            }.runTaskLater(instance, creeper.maxFuseTicks - 5L)
        }
    }
    
    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        val creeper = event.entity as? Creeper ?: return
        val loc = creeper.eyeLocation

        for (i in 0..Random.nextInt(4)) {
            val fish = creeper.world.spawnEntity(loc, EntityType.SILVERFISH) as Silverfish
            fish.health = 1.0
            fish.customName = "クリーパーの残り火"
            fish.isSilent = true
            fish.fireTicks = 20 * 9999
            fish.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 9999, 10, false, false))
            fish.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 20 * 9999, 1, false, false))
            fish.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 9999, 2, false, false))
            object : BukkitRunnable() {
                override fun run() {
                    if (fish.isDead) {
                        cancel()
                    } else {
                        fish.world.spawnParticle(Particle.SMOKE_LARGE, fish.location, 1, .0, .0, .0, .01)
                    }
                }
            }.runTaskTimer(instance, 0, 4)
        }
        for (i in 0..15) {
            val fire = creeper.world.spawnFallingBlock(creeper.eyeLocation, Material.FIRE.createBlockData())
            val multiple = i / 8 + 1
            when (i%8) {
                0 -> {
                    fire.velocity = Vector(.2 * multiple, .1, .0)
                }
                1 -> {
                    fire.velocity = Vector(.0, .1, .2 * multiple)
                }
                2 -> {
                    fire.velocity = Vector(-.2 * multiple, .1, .0)
                }
                3 -> {
                    fire.velocity = Vector(.0, .1, -.2 * multiple)
                }
                4 -> {
                    fire.velocity = Vector(.2 * multiple, .1, .2 * multiple)
                }
                5 -> {
                    fire.velocity = Vector(-.2 * multiple, .1, .2 * multiple)
                }
                6 -> {
                    fire.velocity = Vector(.2 * multiple, .1, -.2 * multiple)
                }
                7 -> {
                    fire.velocity = Vector(-.2 * multiple, .1, -.2 * multiple)
                }
            }
        }
    }
    
    @EventHandler
    fun onChange(event: EntityChangeBlockEvent) {
        val entity = event.entity as? Silverfish ?: return
        if (entity.customName == "クリーパーの残り火") {
            event.isCancelled = true
        }
    }
    
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val creeper = event.entity as? Creeper ?: return
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            creeper.explode()
        }
    }

    @EventHandler
    fun onLand(event: EntityChangeBlockEvent) {
        val entity = event.entity as? FallingBlock ?: return
        if (entity.persistentDataContainer.has(NamespacedKey(instance, "SHAKE_EARTH"), PersistentDataType.BYTE)) {
            event.isCancelled = true
            jumpBlockBackup[entity]?.let { prev->
                prev.location.block.blockData = prev.blockData
            }
            jumpBlockBackup.remove(entity)
            entity.remove()
        }
    }

    @EventHandler
    fun onDropItemFromEntity(event: EntityDropItemEvent) {
        val entity = event.entity as? FallingBlock ?: return
        if (entity.persistentDataContainer.has(NamespacedKey(instance, "SHAKE_EARTH"), PersistentDataType.BYTE)) {
            event.isCancelled = true
            jumpBlockBackup[entity]?.let { prev->
                prev.location.block.blockData = prev.blockData
            }
            jumpBlockBackup.remove(entity)
            entity.remove()
        }
    }
}
