package com.github.kotyabuchi.pumpkingmc.Entity.Monster.Zombie

import com.github.kotyabuchi.pumpkingmc.Entity.Monster.MobExpansionMaster
import com.github.kotyabuchi.pumpkingmc.Utility.isGrass
import com.github.kotyabuchi.pumpkingmc.Utility.isLiquid
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.random.Random

open class EnchantedZombie(private vararg val zombieTypes: EntityType = arrayOf(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER), private val scaffoldBlock: Material = Material.MOSSY_COBBLESTONE): MobExpansionMaster(*zombieTypes) {

    init {
        object : BukkitRunnable() {
            override fun run() {
                instance.server.onlinePlayers.forEach {
                    if (it.gameMode == GameMode.SURVIVAL || it.gameMode == GameMode.ADVENTURE) {
                        it.getNearbyEntities(32.0, 8.0, 32.0).forEach { entity ->
                            if (zombieTypes.contains(entity.type)) {
                                entity as Zombie
                                if (entity.target == null) {
                                    entity.target = it
                                    instance.server.pluginManager.callEvent(EntityTargetLivingEntityEvent(entity, it, EntityTargetEvent.TargetReason.CLOSEST_PLAYER))
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(instance, 0, 40)

        addSpawnAction(0 until 10) { zombie ->
            zombie.world.spawnEntity(zombie.location, zombie.type)
        }
        addSpawnAction { zombie ->
            zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = (zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.value ?: 0.22) + (Random.nextInt(10) / 100.0)
            zombie.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 1.0
            zombie.equipment?.let {
                it.setItemInOffHand(ItemStack(scaffoldBlock))
                if (it.helmet?.type == Material.AIR) {
                    val helmet = ItemStack(Material.LEATHER_HELMET)
                    val meta = helmet.itemMeta as LeatherArmorMeta
                    meta.addEnchant(Enchantment.DEPTH_STRIDER, 1, true)
                    when (zombie.type) {
                        EntityType.HUSK -> meta.setColor(Color.fromRGB(253, 203, 110))
                        EntityType.ZOMBIFIED_PIGLIN -> meta.setColor(Color.fromRGB(255, 118, 117))
                        EntityType.DROWNED -> meta.setColor(Color.fromRGB(0, 206, 201))
                        else -> meta.setColor(Color.fromRGB(39, 174, 96))
                    }
                    helmet.itemMeta = meta
                    it.helmet = helmet
                    it.helmetDropChance = 0f
                }
            }
        }

        addStartFightAction { zombie ->
            val target = zombie.target
            if (target != null) {
                zombie.getNearbyEntities(32.0, 8.0, 32.0).forEach {
                    if (zombieTypes.contains(it.type)) {
                        it as Zombie
                        if (it.target == null) {
                            it.target = target
                            instance.server.pluginManager.callEvent(EntityTargetLivingEntityEvent(it, target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER))
                        }
                    }
                }
            }
        }
        addStartFightAction { zombie ->
            object: BukkitRunnable() {
                override fun run() {
                    val target = zombie.target
                    if (zombie.isDead || target == null) {
                        cancel()
                    } else {
                        val zombieLoc = zombie.location
                        val targetLoc = target.location
                        if (targetLoc.block.y - zombieLoc.block.y > 1 && (zombieLoc.block.type.isAir || zombieLoc.block.type.isLiquid() || zombieLoc.block.type.isGrass())) {
                            zombie.velocity = Vector(.0, .3, .0)
                            zombie.persistentDataContainer.set(NamespacedKey(instance, "Jumped"), PersistentDataType.BYTE, 1)
                            object: BukkitRunnable() {
                                override fun run() {
                                    zombie.persistentDataContainer.remove(NamespacedKey(instance, "Jumped"))
                                    val targetBlock = zombie.location.add(0.0, 0.5, 0.0).block.getRelative(BlockFace.DOWN)
                                    val type= targetBlock.type
                                    if (type.isAir || type.isLiquid() || type.isGrass()) {
                                        targetBlock.type = scaffoldBlock
                                        zombie.world.playSound(targetBlock.location, Sound.BLOCK_STONE_PLACE, .8f, .75f)
                                        val teleportLoc = zombie.location
                                        teleportLoc.y = targetBlock.location.y + 1
                                        zombie.teleport(teleportLoc)
                                        object: BukkitRunnable() {
                                            override fun run() {
                                                if (targetBlock.type == scaffoldBlock) {
                                                    targetBlock.type = Material.AIR
                                                    targetBlock.world.playSound(targetBlock.location, Sound.BLOCK_STONE_BREAK, .8f, .75f)
                                                    targetBlock.world.spawnParticle(Particle.BLOCK_CRACK, targetBlock.location.add(.5, .5, .5), 20, .3, .3, .3, 2.0, scaffoldBlock.createBlockData())
                                                }
                                            }
                                        }.runTaskLater(instance, 20 * 5)
                                    }
                                }
                            }.runTaskLater(instance, 6)
                        }
                    }
                }
            }.runTaskTimer(instance, 30, 12)

            object : BukkitRunnable() {
                override fun run() {
                    val target = zombie.target
                    if (zombie.isDead || target == null) {
                        cancel()
                    } else if (!zombie.persistentDataContainer.has(NamespacedKey(instance, "Jumped"), PersistentDataType.BYTE)) {
                        val zombieLoc = zombie.location
                        val targetLoc = target.location
                        if (targetLoc.block.y >= zombieLoc.block.y) {
                            val lookingDire = zombie.eyeLocation.direction.normalize()
                            lookingDire.y = 0.0
                            val baseBlockLoc = zombieLoc.block.location.add(.5, .0, .5)
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
                                    zombie.velocity = jumpLoc.toVector().subtract(zombieLoc.toVector()).multiply(0.2)
                                    zombie.persistentDataContainer.set(NamespacedKey(instance, "Jumped"), PersistentDataType.BYTE, 1)
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            zombie.persistentDataContainer.remove(NamespacedKey(instance, "Jumped"))
                                        }
                                    }.runTaskLater(instance, 10)
                                } else {
                                    var needPlace = true
                                    var checkBlock = targetBlock
                                    for (i in (0 until 2)) {
                                        val checkType = checkBlock.type
                                        if (checkType.isLiquid()) break
                                        if (!checkType.isAir  && !checkType.isGrass()) {
                                            needPlace = false
                                            break
                                        }
                                        checkBlock = checkBlock.getRelative(BlockFace.DOWN)
                                    }
                                    if (needPlace) {
                                        targetBlock.type = scaffoldBlock
                                        zombie.world.playSound(targetBlock.location, Sound.BLOCK_STONE_PLACE, .8f, .75f)
                                        object: BukkitRunnable() {
                                            override fun run() {
                                                if (targetBlock.type == scaffoldBlock) {
                                                    targetBlock.type = Material.AIR
                                                    targetBlock.world.playSound(targetBlock.location, Sound.BLOCK_STONE_BREAK, .8f, .75f)
                                                    targetBlock.world.spawnParticle(Particle.BLOCK_CRACK, targetBlock.location.add(.5, .5, .5), 20, .3, .3, .3, 2.0, Material.MOSSY_COBBLESTONE.createBlockData())
                                                }
                                            }
                                        }.runTaskLater(instance, 20 * 4)
                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(instance, 0, 2)
        }
    }
    
    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        if (!zombieTypes.contains(entity.type)) return
        entity.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 20 * 3, 1))
        if (Random.nextInt(10) < 1) entity.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 20 * 6, 1))
    }
}
