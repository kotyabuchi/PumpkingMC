package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.BlockBreakJobClass
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.aroundBlockFace
import com.github.kotyabuchi.pumpkingmc.Utility.hasTag
import com.github.kotyabuchi.pumpkingmc.Utility.isOre
import com.github.kotyabuchi.pumpkingmc.Utility.toggleTag
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.round

object Mining: BlockBreakJobClass(JobClassType.MINING) {

    private val stoneSet = setOf(Material.STONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE, Material.NETHERRACK,
        Material.SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CHISELED_SANDSTONE, Material.RED_SANDSTONE,
        Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.MOSSY_COBBLESTONE, Material.BASALT, Material.BLACKSTONE)
    private val mineAssistKey = name + "_MineAssist"
    private val stoneReplacerKey = name + "_StoneReplacer"

    init {
        Material.values().forEach {
            if (it.name.endsWith("_PICKAXE")) addTool(it)
        }
        stoneSet.forEach {
            addExpMap(it, exp = 1)
            addGroundLevelingAssist(it)
        }
        addExpMap(Material.COAL_ORE, exp = 2)
        addExpMap(Material.IRON_ORE, Material.END_STONE, Material.GLOWSTONE, exp = 3)
        addExpMap(Material.REDSTONE_ORE, Material.NETHER_QUARTZ_ORE, exp = 4)
        addExpMap(Material.GOLD_ORE, Material.OBSIDIAN, Material.NETHER_GOLD_ORE, exp = 5)
        addExpMap(Material.LAPIS_ORE, exp = 7)
        addExpMap(Material.DIAMOND_ORE, exp = 8)
        addExpMap(Material.EMERALD_ORE, exp = 10)
        addGroundLevelingAssist(Material.COBBLESTONE)

        addAction(SkillCommand.RRR, 25, fun(player: Player) {
            SuperBreaker.enableSuperBreaker(player, jobClassType)
        })
        addAction(SkillCommand.LLL, 50, fun(player: Player) {
            if (player.toggleTag(stoneReplacerKey)) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aStone Replacer On")
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cStone Replacer Off")
            }
        })
        addAction(SkillCommand.LRL, 50, fun(player: Player) {
            if (player.toggleTag(mineAssistKey)) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aMine Assist On")
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cMine Assist Off")
            }
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
        addAction(SkillCommand.LRR, 200, fun(player: Player) {
            if (player.toggleTag(groundLevelingAssistKey)) {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&aGround Leveling Assist On")
            } else {
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                player.sendActionBar('&', "&cGround Leveling Assist Off")
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
        val playerStatus = player.getStatus()
        val jobClassStatus = playerStatus.getJobClassStatus(jobClassType)
        val item = player.inventory.itemInMainHand

        if (!getTool().contains(item.type)) return
        if (!isTargetBlock(block)) return

        if (block.type.isOre() && player.hasTag(mineAssistKey)) {
            event.isCancelled = true
            val oreList: MutableList<Block> = mutableListOf()
            searchOre(block, oreList, mutableListOf())

            jobClassStatus.addCombo(oreList.size)
            val drops = mutableMapOf<Material, Int>()
            val stoneSlotItem = inv.getItem(inv.heldItemSlot + 1)
            val enableStoneReplacer = player.hasTag(stoneReplacerKey) && inv.getItem(inv.heldItemSlot + 1) != null
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
                instance.server.pluginManager.callEvent(BlockMineEvent(it, player))
            }
            drops.forEach { (t, u) ->
                val dropItem = block.world.dropItemNaturally(block.location.add(0.5, 0.0, 0.5), ItemStack(t, u))
                val dropEvent = BlockDropItemEvent(block, blockState, player, mutableListOf(dropItem))
                instance.server.pluginManager.callEvent(dropEvent)
                if (dropEvent.items.isEmpty()) dropItem.remove()
            }
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
