package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.aroundBlockFace
import com.github.kotyabuchi.pumpkingmc.Utility.isOre
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

object Mining: BlockBreakJobClass(JobClassType.MINING) {

    private val mineAssistKey = NamespacedKey(instance, name + "_MineAssist")
    private val stoneReplacerKey = NamespacedKey(instance, name + "_StoneReplacer")
    private val multiBreakKey = NamespacedKey(instance, name + "_MultiBreak")

    init {
        Material.values().forEach {
            if (it.name.endsWith("_PICKAXE")) addTool(it)
        }
        addExpMap(Material.STONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE, Material.NETHERRACK,
                Material.SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CHISELED_SANDSTONE, Material.RED_SANDSTONE,
                Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.MOSSY_COBBLESTONE, Material.BASALT, Material.BLACKSTONE, exp = 1)
        addExpMap(Material.COAL_ORE, exp = 2)
        addExpMap(Material.IRON_ORE, Material.END_STONE, Material.GLOWSTONE, exp = 3)
        addExpMap(Material.REDSTONE_ORE, Material.NETHER_QUARTZ_ORE, exp = 4)
        addExpMap(Material.GOLD_ORE, Material.OBSIDIAN, Material.NETHER_GOLD_ORE, exp = 5)
        addExpMap(Material.LAPIS_ORE, exp = 7)
        addExpMap(Material.DIAMOND_ORE, exp = 8)
        addExpMap(Material.EMERALD_ORE, exp = 10)

        addAction(SkillCommand.RRR, 25, fun(player: Player) {
            SuperBreaker.enableSuperBreaker(player, jobClassType)
        })
        addAction(SkillCommand.LLL, 50, fun(player: Player) {
            val pdc = player.persistentDataContainer
            if (pdc.has(stoneReplacerKey, PersistentDataType.BYTE)) {
                pdc.remove(stoneReplacerKey)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cStone Replacer Off")
            } else {
                pdc.set(stoneReplacerKey, PersistentDataType.BYTE, 1)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aStone Replacer On")
            }
        })
        addAction(SkillCommand.LRL, 50, fun(player: Player) {
            val pdc = player.persistentDataContainer
            if (pdc.has(mineAssistKey, PersistentDataType.BYTE)) {
                pdc.remove(mineAssistKey)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cMine Assist Off")
            } else {
                pdc.set(mineAssistKey, PersistentDataType.BYTE, 1)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aMine Assist On")
            }
        })
        addAction(SkillCommand.LLR, 100, fun(player: Player) {
            val pdc = player.persistentDataContainer
            if (pdc.has(multiBreakKey, PersistentDataType.BYTE)) {
                pdc.remove(multiBreakKey)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cMulti Break Off")
            } else {
                pdc.set(multiBreakKey, PersistentDataType.BYTE, 1)
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aMulti Break On")
            }
        })
    }

    @EventHandler
    fun onMine(event: BlockMineEvent) {
        val block = event.block
        val blockState = block.state
        val player = event.player
        val playerLoc = player.location
        val inv = player.inventory
        val pdc = player.persistentDataContainer
        val playerStatus = player.getStatus()
        val jobClassStatus = playerStatus.getJobClassStatus(jobClassType)
        val level = jobClassStatus.getLevel()
        val item = player.inventory.itemInMainHand
        if (!getTool().contains(item.type)) return
        if (!isTargetBlock(block)) return

        addBrokenBlockList(block)

        if (block.type.isOre() && pdc.has(mineAssistKey, PersistentDataType.BYTE)) {
            event.isCancelled = true
            val oreList: MutableList<Block> = mutableListOf()
            searchOre(block, oreList, mutableListOf())

            jobClassStatus.addCombo(oreList.size)
            val drops = mutableMapOf<Material, Int>()
            val stoneSlotItem = inv.getItem(inv.heldItemSlot + 1)
            val enableStoneReplacer = pdc.has(mineAssistKey, PersistentDataType.BYTE) && inv.getItem(inv.heldItemSlot + 1) != null
            oreList.sortWith { block1, block2 ->
                round(block2.location.distance(playerLoc) - block1.location.distance(playerLoc)).toInt()
            }
            oreList.forEach {
                for (drop in it.getDrops(item, player)) {
                    if (drop != null && !drop.type.isAir) {
                        drops[drop.type] = (drops[drop.type] ?: 0) + drop.amount
                    }
                }
                it.world.playSound(it.location, it.soundGroup.breakSound, 1f, .75f)
                it.world.spawnParticle(Particle.BLOCK_CRACK, it.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, 2.0, it.blockData)
                if (enableStoneReplacer && stoneSlotItem != null && stoneSlotItem.amount > 0 && (stoneSlotItem.type == Material.STONE || stoneSlotItem.type == Material.COBBLESTONE)) {
                    if (event.isMultiBreak && block.location == it.location) {
                        it.type = Material.AIR
                    } else {
                        it.type = stoneSlotItem.type
                        stoneSlotItem.amount--
                    }
                } else {
                    it.type = Material.AIR
                }
//                if ((event.isMultiBreak && block.location != it.location) && enableStoneReplacer && stoneSlotItem != null && stoneSlotItem.amount > 0 && (stoneSlotItem.type == Material.STONE || stoneSlotItem.type == Material.COBBLESTONE)) {
//                    it.type = stoneSlotItem.type
//                    stoneSlotItem.amount--
//                } else {
//                    it.type = Material.AIR
//                }
                instance.server.pluginManager.callEvent(BlockMineEvent(it, player))
            }
            drops.forEach { (t, u) ->
                val dropItem = block.world.dropItemNaturally(block.location.add(0.5, 0.0, 0.5), ItemStack(t, u))
                val dropEvent = BlockDropItemEvent(block, blockState, player, mutableListOf(dropItem))
                instance.server.pluginManager.callEvent(dropEvent)
                if (dropEvent.items.isEmpty()) dropItem.remove()
            }
        }

        if (level >= 100 && Random.nextInt(1000) < min(1000, level) / 2) {
            val itemExpansion = ItemExpansion(item)
            val mendingAmount = floor(level / 100.0).toInt()
            itemExpansion.increaseDurability(mendingAmount)
            player.inventory.setItemInMainHand(itemExpansion.item)
            player.sendActionMessage("&dSoftTouch Mending +$mendingAmount")
        }
    }
    
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        val block = event.block
        val player = event.player
        val level = player.getStatus().getJobClassStatus(jobClassType).getLevel()
        val pdc = player.persistentDataContainer
        val item = player.inventory.itemInMainHand
        if (!getTool().contains(item.type)) return

        if (pdc.has(multiBreakKey, PersistentDataType.BYTE)) {
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
                            if (checkBlock.type == Material.BEDROCK) continue
                            if (checkBlock.type.isAir) continue
                            blockList.add(checkBlock)
                        }
                    }
                }
                BlockFace.SOUTH, BlockFace.NORTH -> {
                    for (x in range) {
                        for (y in -1 until (radius * 2)) {
                            val checkBlock = block.location.add(x.toDouble(), y.toDouble(), 0.0).block
                            if (checkBlock.y <= 1 || checkBlock.type == Material.BEDROCK) continue
                            if (checkBlock.type.isAir) continue
                            blockList.add(checkBlock)
                        }
                    }
                }
                BlockFace.EAST, BlockFace.WEST -> {
                    for (z in range) {
                        for (y in -1 until (radius * 2)) {
                            val checkBlock = block.location.add(0.0, y.toDouble(), z.toDouble()).block
                            if (checkBlock.y <= 1 || checkBlock.type == Material.BEDROCK) continue
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
                    val damageChance = 100 / (item.getEnchantmentLevel(Enchantment.DURABILITY) + 1)
                    if (Random.nextInt(100) <= damageChance) {
                        instance.server.pluginManager.callEvent(PlayerItemDamageEvent(player, item, 1))
                    }
                }
            }
        } else {
            val mineEvent = BlockMineEvent(block, player)
            instance.server.pluginManager.callEvent(mineEvent)
            if (mineEvent.isCancelled) event.isCancelled = true
        }
    }

    private fun searchOre(checkBlock: Block, oreList: MutableList<Block>, checkedList: MutableList<Block>) {
        if (checkedList.contains(checkBlock)) return
        checkedList.add(checkBlock)
        val checkMaterial = checkBlock.type
        if (!checkMaterial.isOre()) return
        oreList.add(checkBlock)
        if (checkedList.size > 512) return
        val upBlock = checkBlock.getRelative(BlockFace.UP)
        val downBlock = checkBlock.getRelative(BlockFace.DOWN)
        aroundBlockFace.forEach {
            searchOre(upBlock.getRelative(it), oreList, checkedList)
        }
        aroundBlockFace.forEach {
            searchOre(checkBlock.getRelative(it), oreList, checkedList)
        }
        aroundBlockFace.forEach {
            searchOre(downBlock.getRelative(it), oreList, checkedList)
        }
    }
}
