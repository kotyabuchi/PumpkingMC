package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.Enum.WoodType
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.aroundBlockFace
import com.github.kotyabuchi.pumpkingmc.Utility.getWoodType
import com.github.kotyabuchi.pumpkingmc.Utility.isLeave
import com.github.kotyabuchi.pumpkingmc.Utility.isWood
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Leaves
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

object Woodcutting: BlockBreakJobClass("WOOD_CUTTING") {
    
    private val treeAssistKey = NamespacedKey(instance, jobClassName + "_TreeAssist")
    private val treeAssistMap = mutableMapOf<Player, BukkitTask>()
    
    private val logFallKey = NamespacedKey(instance, jobClassName + "_LogFall")

    init {
        Material.values().forEach {
            if (it.name.endsWith("_AXE")) addTool(it)
            if (it.isWood()) addExpMap(it, exp = 1)
        }
        addAction(SkillCommand.LRL, 50, fun(player: Player) {
            val pdc = player.persistentDataContainer
            if (pdc.has(treeAssistKey, PersistentDataType.BYTE)) {
                pdc.remove(treeAssistKey)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cTree Assist Off")
            } else {
                pdc.set(treeAssistKey, PersistentDataType.BYTE, 1)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aTree Assist On")
                treeAssistMap[player] = object : BukkitRunnable() {
                    override fun run() {
                        pdc.remove(treeAssistKey)
                        player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                        player.sendActionBar('&', "&cTree Assist Off")
                    }
                }.runTaskLater(instance, 20 * 6)
            }
        })
        addAction(SkillCommand.LLL, 0, fun(player: Player) {
            val pdc = player.persistentDataContainer
            if (pdc.has(logFallKey, PersistentDataType.BYTE)) {
                pdc.remove(logFallKey)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aTree Fall On")
            } else {
                pdc.set(logFallKey, PersistentDataType.BYTE, 1)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cTree Fall Off")
            }
        })
    }
    
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        val block = event.block
        val material = block.type
        val blockState = block.state
        val player = event.player
        val playerStatus = player.getStatus()
        val jobClassStatus = playerStatus.getJobClassStatus(this)
        val item = player.inventory.itemInMainHand
        val pdc = player.persistentDataContainer
        if (!material.isWood()) return

        val y = block.y
        if (getTool().contains(item.type) && pdc.has(treeAssistKey, PersistentDataType.BYTE)) {
            val woodList: MutableList<Block> = mutableListOf()
            val leaveList: MutableList<Block> = mutableListOf()
            searchWood(block, block, woodList, leaveList, mutableListOf())
            treeAssistMap[player]?.cancel()
            treeAssistMap[player] = object : BukkitRunnable() {
                override fun run() {
                    pdc.remove(treeAssistKey)
                    player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                    player.sendActionBar('&', "&cTree Assist Off")
                }
            }.runTaskLater(instance, 20 * 6)

            leaveList.sortWith { o1, o2 -> o1.y - o2.y }
            jobClassStatus.addCombo(woodList.size - 1)
            val drops = mutableMapOf<Material, Int>()
            woodList.forEach {
                for (drop in it.drops) {
                    if (drop != null && !drop.type.isAir) {
                        drops[drop.type] = (drops[drop.type] ?: 0) + drop.amount
                    }
                }
                addBrokenBlockSet(it)
                it.world.playSound(it.location, Sound.BLOCK_WOOD_BREAK, 1f, 1f)
                it.world.spawnParticle(Particle.BLOCK_CRACK, it.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, 2.0, it.blockData)
                it.type = Material.AIR

                val mineEvent = BlockMineEvent(it, player)
                instance.server.pluginManager.callEvent(mineEvent)
                event.isCancelled = true
                if (!mineEvent.isCancelled) {
                    it.getDrops(item, player).forEach { item ->
                        val dropItem = block.world.dropItem(block.location.add(0.5, 0.0, 0.5), item)
                        val dropEvent = BlockDropItemEvent(it, it.state, player, mutableListOf(dropItem))
                        instance.server.pluginManager.callEvent(dropEvent)
                        if (dropEvent.items.isEmpty()) dropItem.remove()
                    }
                    it.world.playSound(it.location, Sound.BLOCK_WOOD_BREAK, 1f, 1f)
                    it.world.spawnParticle(Particle.BLOCK_CRACK, it.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, 2.0, it.blockData)
                    it.type = Material.AIR
                }
            }
            drops.forEach { (t, u) ->
                val dropItem = block.world.dropItem(block.location.add(0.5, 0.0, 0.5), ItemStack(t, u))
                val dropEvent = BlockDropItemEvent(block, blockState, player, mutableListOf(dropItem))
                instance.server.pluginManager.callEvent(dropEvent)
                if (dropEvent.items.isEmpty()) dropItem.remove()
            }
            leaveList.forEach {
                object : BukkitRunnable() {
                    override fun run() {
                        block.world.spawnFallingBlock(it.location.add(0.5, 0.0, 0.5), it.blockData)
                        it.type = Material.AIR
                    }
                }.runTaskLater(instance, it.y - y.toLong())
            }
        } else {
            if (getTool().contains(item.type)) {
                val mineEvent = BlockMineEvent(block, player)
                instance.server.pluginManager.callEvent(mineEvent)
                if (mineEvent.isCancelled) event.isCancelled = true
            }

            if (!pdc.has(logFallKey, PersistentDataType.BYTE)) {
                val woodList: MutableList<Block> = mutableListOf()
                val leaveList: MutableList<Block> = mutableListOf()
                searchWood(block, block, woodList, leaveList, mutableListOf())
                woodList.remove(block)
                woodList.addAll(leaveList)
                woodList.sortWith { o1, o2 -> o1.y - o2.y }
                woodList.forEach {
                    object : BukkitRunnable() {
                        override fun run() {
                            if (it.type.isLeave()) {
                                block.world.spawnFallingBlock(it.location.add(0.5, 0.0, 0.5), it.blockData)
                                it.type = Material.AIR
                            } else if (!it.getRelative(BlockFace.DOWN).type.isSolid) {
                                block.world.spawnFallingBlock(it.location.add(0.5, 0.0, 0.5), it.blockData)
                                it.type = Material.AIR
                            }
                        }
                    }.runTaskLater(instance, it.y - y.toLong())
                }
                if (getTool().contains(item.type)) addBrokenBlockSet(block)
            }
        }
    }
    
    @EventHandler
    fun onLand(event: EntityChangeBlockEvent) {
        val entity = event.entity as? FallingBlock ?: return
        val material = entity.blockData.material
        if (material.isLeave()) {
            dropFromLeave(entity)
            event.isCancelled = true
            entity.remove()
        }
    }
    
    @EventHandler
    fun onDropItemFromEntity(event: EntityDropItemEvent) {
        val entity = event.entity as? FallingBlock ?: return
        val material = entity.blockData.material
        if (material.isLeave()) {
            dropFromLeave(entity)
            event.isCancelled = true
            entity.remove()
        }
    }

    private fun dropFromLeave(entity: FallingBlock) {
        val material = entity.blockData.material
        if (Random.nextInt(100) < 5) {
            val item = entity.world.dropItem(entity.location, ItemStack(Material.valueOf(material.name.replace("LEAVES", "SAPLING"))))
            instance.server.pluginManager.callEvent(EntityDropItemEvent(entity, item))
        }
        if (Random.nextInt(1000) < 15) {
            val item = entity.world.dropItem(entity.location, ItemStack(Material.STICK, Random.nextInt(2) + 1))
            instance.server.pluginManager.callEvent(EntityDropItemEvent(entity, item))
        }
        if (material.name.contains("OAK") && Random.nextInt(1000) < 5) {
            val item = entity.world.dropItem(entity.location, ItemStack(Material.APPLE, 1))
            instance.server.pluginManager.callEvent(EntityDropItemEvent(entity, item))
        }
    }
    
    private fun searchWood(mainBlock: Block, checkBlock: Block, woodList: MutableList<Block>, leaveList: MutableList<Block>, checkedList: MutableList<Block>) {
        if (checkedList.contains(checkBlock)) return
        checkedList.add(checkBlock)
        if (mainBlock.location.y > checkBlock.location.y) return
        val mainMaterial = mainBlock.type
        val checkMaterial = checkBlock.type
        if (!checkMaterial.isWood()) {
            if (checkMaterial.isLeave()) searchLeave(mainBlock, checkBlock, checkMaterial.getWoodType()!!, leaveList, 0)
            return
        }
        if (mainMaterial.getWoodType() != checkMaterial.getWoodType()) return
        woodList.add(checkBlock)
        if (checkedList.size > 496) return
        val upBlock = checkBlock.getRelative(BlockFace.UP)
        val downBlock = checkBlock.getRelative(BlockFace.DOWN)
        aroundBlockFace.forEach {
            searchWood(mainBlock, upBlock.getRelative(it), woodList, leaveList, checkedList)
        }
        aroundBlockFace.forEach {
            searchWood(mainBlock, checkBlock.getRelative(it), woodList, leaveList, checkedList)
        }
        aroundBlockFace.forEach {
            searchWood(mainBlock, downBlock.getRelative(it), woodList, leaveList, checkedList)
        }
    }
    
    private fun searchLeave(beforeBlock: Block, checkBlock: Block, woodType: WoodType, list: MutableList<Block>, _count: Int) {
        var count = _count
        if (list.contains(checkBlock)) return
        val checkMaterial = checkBlock.type
        if (!checkMaterial.isLeave()) return
        val leaves = checkBlock.blockData as Leaves
        if (checkMaterial.getWoodType() !== woodType) return
        if (leaves.isPersistent) return
        if (beforeBlock.type.isLeave() && (beforeBlock.blockData as Leaves).distance >= leaves.distance) return
        list.add(checkBlock)
        count++
        if (count > 496) return
        val upBlock = checkBlock.getRelative(BlockFace.UP)
        val downBlock = checkBlock.getRelative(BlockFace.DOWN)
        aroundBlockFace.forEach {
            searchLeave(checkBlock, upBlock.getRelative(it), woodType, list, count)
        }
        aroundBlockFace.forEach {
            searchLeave(checkBlock, checkBlock.getRelative(it), woodType, list, count)
        }
        aroundBlockFace.forEach {
            searchLeave(checkBlock, downBlock.getRelative(it), woodType, list, count)
        }
    }
}
