package com.github.kotyabuchi.pumpkingmc.Class

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.*
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

open class BlockBreakJobClass(jobClassType: JobClassType): JobClassMaster(jobClassType) {

    private val expMap = mutableMapOf<Material, Int>()
    private val brokenBlockSet = mutableSetOf<Block>()
    private val placedBlock = mutableMapOf<Block, BukkitTask>()
    private val groundLevelingAssisBlockSet = mutableSetOf<Material>()
    val multiBreakKey = name + "_MultiBreak"
    val groundLevelingAssistKey = name + "_GroundLevelingAssist"

    fun addExpMap(vararg materials: Material, exp: Int) {
        materials.forEach {
            expMap[it] = exp
        }
    }

    fun isTargetBlock(block: Block): Boolean {
        return expMap.containsKey(block.type)
    }

    fun addBrokenBlockSet(block: Block) {
        brokenBlockSet.add(block)
    }

    fun addGroundLevelingAssist(vararg materials: Material) {
        groundLevelingAssisBlockSet.addAll(materials)
    }

    fun containsBrokenBlockSet(block: Block): Boolean {
        return brokenBlockSet.contains(block)
    }

    open fun afterDropAction(event: BlockDropItemEvent) {}

    @EventHandler(priority = EventPriority.HIGH)
    fun onDropItemFromBlock(event: BlockDropItemEvent) {
        val block = event.block
        val blockState = event.blockState
        val player = event.player
        val playerStatus = player.getStatus()

        if (brokenBlockSet.contains(block)) {
            if (placedBlock.containsKey(block)) {
                placedBlock[block]?.cancel()
                placedBlock.remove(block)
            } else {
                var exp = 0.0
                val doubleDropChance = playerStatus.getJobClassStatus(jobClassType).getLevel() / 3
                var dropAmount = 1 + floor(doubleDropChance / 100.0).toInt()
                if (Random.nextInt(100) < doubleDropChance % 100) {
                    dropAmount++
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.0f)
                }
                event.items.forEach {
                    val item = it.itemStack
                    val itemExp = expMap[blockState.type] ?: 1
                    item.amount *= dropAmount
                    exp += (itemExp * item.amount)
                }
                if (SuperBreaker.isSuperBreaking(player)) exp *= 1.5

                playerStatus.addSkillExp(jobClassType, exp)
                afterDropAction(event)
            }
        }
        brokenBlockSet.remove(block)
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        val block = event.block
        placedBlock[block]?.cancel()
        placedBlock[block] = object : BukkitRunnable() {
            override fun run() {
                placedBlock.remove(block)
            }
        }.runTaskLater(instance, 20 * 10)
    }

    @EventHandler
    fun onBlockMine(event: BlockMineEvent) {
        val player = event.player
        val playerStatus = player.getStatus()
        val block = event.block
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type)) return
        if (!isTargetBlock(block)) return
        addBrokenBlockSet(block)

        val level = playerStatus.getJobClassStatus(jobClassType).getLevel()
        if (item.itemMeta is Damageable) {
            if (level >= 100 && Random.nextInt(1000) < min(1000, level) / 2) {
                val itemExpansion = ItemExpansion(item)
                val mendingAmount = floor(level / 100.0).toInt()
                itemExpansion.increaseDurability(mendingAmount)
                player.inventory.setItemInMainHand(itemExpansion.item)
                player.sendActionMessage("&dSoftTouch Mending +$mendingAmount")
            } else {
                player.inventory.itemInMainHand.damage(player, 1)
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        val player = event.player
        val level = player.getStatus().getJobClassStatus(jobClassType).getLevel()
        val block = event.block
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type)) return

        event.isCancelled = true

        if (player.hasTag(groundLevelingAssistKey)) {
            val rayBlock = player.rayTraceBlocks(8.0)
            rayBlock?.hitBlockFace?.reverse()?.let { lookingFace->
                val targetBlock = block.getRelative(lookingFace)
                if (targetBlock.type.isAir) {
                    for (groundLevelingMaterial in groundLevelingAssisBlockSet) {
                        if (player.inventory.consume(ItemExpansion(ItemStack(groundLevelingMaterial)).item)) {
                            targetBlock.type = groundLevelingMaterial
                            break
                        }
                    }
                }
            }
        }

        val blockList = mutableListOf(block)
        if (player.hasTag(multiBreakKey)) {
            val rayBlock = player.rayTraceBlocks(6.0)?: return
            val lookingFace = rayBlock.hitBlockFace ?: return
            val radius = floor(level / 100.0).toInt()
            val range = (radius * -1)..radius
            when (lookingFace) {
                BlockFace.UP, BlockFace.DOWN -> {
                    for (x in range) {
                        for (z in range) {
                            val checkBlock = block.location.add(x.toDouble(), 0.0, z.toDouble()).block
                            if (checkBlock.getDrops(item, player).isEmpty()) continue
                            if (checkBlock.type.isAir) continue
                            blockList.add(checkBlock)
                        }
                    }
                }
                BlockFace.SOUTH, BlockFace.NORTH -> {
                    for (x in range) {
                        for (y in -1 until (radius * 2)) {
                            val checkBlock = block.location.add(x.toDouble(), y.toDouble(), 0.0).block
                            if (checkBlock.getDrops(item, player).isEmpty()) continue
                            if (checkBlock.type.isAir) continue
                            blockList.add(checkBlock)
                        }
                    }
                }
                BlockFace.EAST, BlockFace.WEST -> {
                    for (z in range) {
                        for (y in -1 until (radius * 2)) {
                            val checkBlock = block.location.add(0.0, y.toDouble(), z.toDouble()).block
                            if (checkBlock.getDrops(item, player).isEmpty()) continue
                            if (checkBlock.type.isAir) continue
                            blockList.add(checkBlock)
                        }
                    }
                }
                else -> {
                    player.sendMessage("Error")
                }
            }
        }
        blockList.forEach {
            val mineEvent = BlockMineEvent(it, player, true)
            instance.server.pluginManager.callEvent(mineEvent)
            if (!mineEvent.isCancelled) {
                it.getDrops(item, player).forEach { item ->
                    val dropItem = block.world.dropItemNaturally(block.location.add(0.5, 0.0, 0.5), item)
                    val dropEvent = BlockDropItemEvent(it, it.state, player, mutableListOf(dropItem))
                    instance.server.pluginManager.callEvent(dropEvent)
                    if (dropEvent.items.isEmpty()) dropItem.remove()
                }
                it.world.playSound(it.location, it.soundGroup.breakSound, 1f, .75f)
                it.world.spawnParticle(Particle.BLOCK_CRACK, it.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, .0, it.blockData)
                it.type = Material.AIR
            }
        }
    }
}