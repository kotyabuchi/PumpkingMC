package com.github.kotyabuchi.pumpkingmc.Class

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getJobClassLevel
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.damage
import com.github.kotyabuchi.pumpkingmc.Utility.hasTag
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

open class BlockBreakJobClass(jobClassName: String): JobClassMaster(jobClassName) {
    private val expMap = mutableMapOf<Material, Int>()
    private val brokenBlockSet = mutableSetOf<Block>()
    private val canGetExpWithHand = true
    private val placedBlock = mutableMapOf<Block, BukkitTask>()
    val multiBreakKey = jobClassName + "_MultiBreak"

    init {
        addAction(SkillCommand.RRR, 25, fun(player: Player) {
            SuperBreaker.toggleSkill(player, player.getJobClassLevel(this))
        })
    }

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
                val doubleDropChance = playerStatus.getJobClassStatus(this).getLevel() / 3
                var multiDropAmount = 1 + floor(doubleDropChance / 100.0).toInt()
                if (Random.nextInt(100) < doubleDropChance % 100) {
                    multiDropAmount++
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.0f)
                }
                val itemExp = expMap[blockState.type] ?: 1
                event.items.forEach {
                    val item = it.itemStack
                    item.amount *= multiDropAmount
                    exp += (itemExp * item.amount)
                }
                if (SuperBreaker.isEnabledSkill(player)) exp *= 1.5

                playerStatus.addSkillExp(this, exp, multiDropAmount)
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

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockMine(event: BlockMineEvent) {
        if (event.isCancelled) return
        val player = event.player
        val playerStatus = player.getStatus()
        val block = event.block
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type) && (canGetExpWithHand && !item.type.isAir)) return
        if (!isTargetBlock(block)) return
        addBrokenBlockSet(block)

        val level = playerStatus.getJobClassStatus(this).getLevel()
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

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        val player = event.player
        val block = event.block
        val itemStack = player.inventory.itemInMainHand

        val usingTool = getTool().contains(itemStack.type)

        if (!usingTool && (canGetExpWithHand && !itemStack.type.isAir)) return

        event.isCancelled = true

        val mineEvent = BlockMineEvent(block, player, player.hasTag(multiBreakKey))
        instance.server.pluginManager.callEvent(mineEvent)
        if (!mineEvent.isCancelled) {
            val dropItems = mutableListOf<Item>()
            block.getDrops(itemStack, player).forEach { item ->
                val dropItem = block.world.dropItem(block.location.add(0.5, 0.0, 0.5), item)
                dropItems.add(dropItem)
            }
            val state = block.state
            block.type = Material.AIR
            val dropEvent = BlockDropItemEvent(block, state, player, dropItems)
            instance.server.pluginManager.callEvent(dropEvent)
            if (dropEvent.items.isEmpty()) {
                dropItems.forEach { item ->
                    item.remove()
                }
            }
        }
    }
}