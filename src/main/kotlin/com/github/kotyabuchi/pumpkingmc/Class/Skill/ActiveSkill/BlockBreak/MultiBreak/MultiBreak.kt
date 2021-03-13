package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.floor

open class MultiBreak: ActiveSkillMaster {
    override val skillName: String = "MultiBreak"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 100
    override var description: String = "レベルに応じた範囲を一括破壊する"
    override val hasActiveTime: Boolean = false
    override val activePlayerLevelMap: MutableMap<UUID, Int> = mutableMapOf()
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val coolTimePlayers: MutableList<UUID> = mutableListOf()
    override fun calcActiveTime(level: Int): Int = 0

    private val targetTool: MutableSet<Material> = mutableSetOf()
    private val transparentBlocks = mutableSetOf<Material>()

    init {
        Material.values().forEach {
            if (it.isBlock && !it.isSolid && !it.isOccluding) transparentBlocks.add(it)
        }
    }

    fun addTool(vararg materials: Material) {
        targetTool.addAll(materials)
    }

    override fun enableAction(player: Player, level: Int) {
        player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        player.sendActionBar(Component.text("$skillName On", NamedTextColor.GREEN))
    }

    override fun disableAction(player: Player) {
        player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        player.sendActionBar(Component.text("$skillName Off", NamedTextColor.RED))
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        if (event.isCancelled) return

        val player = event.player
        val uuid = player.uniqueId

        if (!isEnabledSkill(player)) return

        val level = activePlayerLevelMap[uuid] ?: return
        val block = event.block
        val itemStack = player.inventory.itemInMainHand

        if (!targetTool.contains(itemStack.type)) return

        event.isCancelled = true
        val blockList = getBlocks(block, player, itemStack, level)
        blockList.remove(block)

        blockList.forEach {
            val mineEvent = BlockMineEvent(it, player, true)
            instance.server.pluginManager.callEvent(mineEvent)
            if (!mineEvent.isCancelled) {
                val dropItems = mutableListOf<Item>()
                it.getDrops(itemStack, player).forEach { item ->
                    val dropItem = block.world.dropItemNaturally(block.location.add(0.5, 0.0, 0.5), item)
                    dropItems.add(dropItem)
                }
                val state = it.state
                if (it != block) {
                    it.world.playSound(it.location.add(.5, .5, .5), it.soundGroup.breakSound, 1f, .75f)
                    it.world.spawnParticle(Particle.BLOCK_CRACK, it.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, .0, it.blockData)
                }
                it.type = Material.AIR
                val dropEvent = BlockDropItemEvent(it, state, player, dropItems)
                instance.server.pluginManager.callEvent(dropEvent)
                if (dropEvent.items.isEmpty()) {
                    dropItems.forEach { item ->
                        item.remove()
                    }
                }
            }
        }
    }

    open fun getBlocks(block: Block, player: Player, itemStack: ItemStack, level: Int): MutableList<Block> {
        val result = mutableListOf<Block>()
        val rayBlock = player.rayTraceBlocks(6.0)?: return result
        val lookingFace = rayBlock.hitBlockFace ?: return result
        val radius = floor(level / 100.0).toInt()
        val range = (radius * -1)..radius
        when (lookingFace) {
            BlockFace.UP, BlockFace.DOWN -> {
                for (x in range) {
                    for (z in range) {
                        val checkBlock = block.location.add(x.toDouble(), 0.0, z.toDouble()).block
                        if (checkBlock.type.isAir) continue
                        if (!checkBlock.isValidTool(itemStack)) continue
                        result.add(checkBlock)
                    }
                }
            }
            BlockFace.SOUTH, BlockFace.NORTH -> {
                for (x in range) {
                    for (y in -1 until (radius * 2)) {
                        val checkBlock = block.location.add(x.toDouble(), y.toDouble(), 0.0).block
                        if (checkBlock.type.isAir) continue
                        if (!checkBlock.isValidTool(itemStack)) continue
                        result.add(checkBlock)
                    }
                }
            }
            BlockFace.EAST, BlockFace.WEST -> {
                for (z in range) {
                    for (y in -1 until (radius * 2)) {
                        val checkBlock = block.location.add(0.0, y.toDouble(), z.toDouble()).block
                        if (checkBlock.type.isAir) continue
                        if (!checkBlock.isValidTool(itemStack)) continue
                        result.add(checkBlock)
                    }
                }
            }
            else -> {
                player.sendMessage("Error")
            }
        }
        return result
    }
}