package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.hasTag
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.Utility.toggleTag
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.persistence.PersistentDataType
import java.util.ArrayList
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

object Excavation: BlockBreakJobClass(JobClassType.EXCAVATION) {

    private val multiBreakKey = name + "_MultiBreak"

    init {
        Material.values().forEach {
            if (it.name.endsWith("_SHOVEL")) addTool(it)
        }
        addExpMap(Material.CLAY, exp = 2)

        addAction(SkillCommand.RRR, 25, fun(player: Player) {
            SuperBreaker.enableSuperBreaker(player, jobClassType)
        })
        addAction(SkillCommand.LLR, 100, fun(player: Player) {
            if (player.toggleTag(multiBreakKey)) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aMulti Break On")
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cMulti Break Off")
            }
        })
    }

    @EventHandler
    fun onMine(event: BlockMineEvent) {
        val player = event.player
        val playerStatus = player.getStatus()
        val block = event.block
        val type = block.type
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type)) return
        if (type == Material.DIRT || type == Material.SAND || type == Material.GRASS_BLOCK || type == Material.GRAVEL || type == Material.CLAY ||
            type == Material.FARMLAND || type == Material.GRASS_PATH || type == Material.COARSE_DIRT || type == Material.PODZOL ||
            type == Material.RED_SAND || type == Material.SOUL_SAND || type == Material.SOUL_SOIL) {

            addBrokenBlockList(block)

            val level = playerStatus.getJobClassStatus(jobClassType).getLevel()
            if (level >= 100 && Random.nextInt(1000) < min(1000, level) / 2) {
                val itemExpansion = ItemExpansion(item)
                val mendingAmount = floor(level / 100.0).toInt()
                itemExpansion.increaseDurability(mendingAmount)
                player.inventory.setItemInMainHand(itemExpansion.item)
                player.sendActionMessage("&dSoftTouch Mending +$mendingAmount")
            }
        }
    }
    
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        val player = event.player
        val level = player.getStatus().getJobClassStatus(jobClassType).getLevel()
        val block = event.block
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type)) return

        if (player.hasTag(multiBreakKey)) {
            val blockList = ArrayList<Block>()
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
            blockList.forEach {
                val mineEvent = BlockMineEvent(it, player, true)
                instance.server.pluginManager.callEvent(mineEvent)
                event.isCancelled = true
                if (!mineEvent.isCancelled) {
                    it.getDrops(item, player).forEach { item ->
                        val dropItem = block.world.dropItemNaturally(block.location.add(0.5, 0.0, 0.5), item)
                        val dropEvent = BlockDropItemEvent(it, it.state, player, mutableListOf(dropItem))
                        instance.server.pluginManager.callEvent(dropEvent)
                        if (dropEvent.items.isEmpty()) dropItem.remove()
                    }
                    it.world.playSound(it.location, it.soundGroup.breakSound, 1f, .75f)
                    it.world.spawnParticle(Particle.BLOCK_CRACK, it.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, 2.0, it.blockData)
                    it.type = Material.AIR
                }
            }
        } else {
            val mineEvent = BlockMineEvent(block, player)
            instance.server.pluginManager.callEvent(mineEvent)
            if (mineEvent.isCancelled) event.isCancelled = true
        }
    }
}
