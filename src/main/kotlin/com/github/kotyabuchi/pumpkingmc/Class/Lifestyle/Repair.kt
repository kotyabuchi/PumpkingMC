package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object Repair: JobClassMaster("REPAIR") {
    
    @EventHandler
    fun onClickAnvil(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val player = event.player
        val block = event.clickedBlock ?: return
        
        if (!block.type.name.endsWith("ANVIL")) return
        val underBlock = block.getRelative(BlockFace.DOWN)
        val anvilTier = when (underBlock.type) {
            Material.SMOOTH_STONE -> 1
            Material.IRON_BLOCK -> 2
            Material.GOLD_BLOCK -> 3
            Material.DIAMOND_BLOCK -> 4
            Material.NETHERITE_BLOCK -> 10
            else -> return
        }
        event.isCancelled = true
        
        val repairItem = player.inventory.itemInMainHand
        val needItem: Material
        val needTier: Int
        val baseExp: Double
        when (repairItem.type) {
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS -> {
                needItem = Material.LEATHER
                needTier = 1
                baseExp = 50.0
            }
            Material.STONE_AXE, Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SHOVEL, Material.STONE_SWORD -> {
                needItem = Material.COBBLESTONE
                needTier = 1
                baseExp = 40.0
            }
            Material.IRON_AXE, Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_SWORD, 
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS -> {
                needItem = Material.IRON_INGOT
                needTier = 2
                baseExp = 70.0
            }
            Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_SWORD,
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS -> {
                needItem = Material.GOLD_INGOT
                needTier = 3
                baseExp = 130.0
            }
            Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_SWORD,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS -> {
                needItem = Material.DIAMOND
                needTier = 4
                baseExp = 110.0
            }
            Material.NETHERITE_AXE, Material.NETHERITE_HOE, Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_SWORD,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS -> {
                needItem = Material.NETHERITE_INGOT
                needTier = 10
                baseExp = 300.0
            }
            else -> {
                player.sendMessage("AnvilTier: $anvilTier")
                return
            }
        }
    
        val damageable = repairItem.itemMeta as? Damageable ?: return
        if (damageable.damage == 0) return

        val playerStatus = player.getStatus()
        val level = playerStatus.getJobClassStatus(this).getLevel()
        val needLevel = (needTier - 1) * 20
        if (level < needLevel) {
            player.sendActionBar('&', "&cNot enough levels (Need Lv.$needLevel)")
            return
        }
        
        if (anvilTier < needTier) {
            player.sendActionBar('&', "&cNot enough Anvil Tier (Need Tier.$needTier)")
            return
        }
        
        if (player.inventory.contains(needItem)) {
            for ((index, item) in player.inventory.contents.withIndex()) {
                if (item != null && item.type == needItem) {
                    item.amount--
                    player.inventory.setItem(index, item)
                    break
                }
            }
        } else {
            player.sendActionBar('&', "&cNot enough material (Need ${needItem.name})")
            return
        }
        
        var repairMultiple = (level / 500) + 1
        if ((level - (500 * (repairMultiple - 1))) / 5 > Random.nextInt(100)) repairMultiple++
        playerStatus.addSkillExp(this, baseExp * repairMultiple * anvilTier)
        
        var repairAmount = 0
        val name = repairItem.type.name
        when {
            name.endsWith("_SHOVEL") -> repairAmount = 1
            name.endsWith("_SWORD") || name.endsWith("_HOE") -> repairAmount = 2
            name.endsWith("_AXE") || name.endsWith("_PICKAXE") -> repairAmount = 3
            name.endsWith("_BOOTS") -> repairAmount = 4
            name.endsWith("_HELMET") -> repairAmount = 5
            name.endsWith("_LEGGINGS") -> repairAmount = 7
            name.endsWith("_CHESTPLATE") -> repairAmount = 8
        }
        
        if (level < Random.nextInt(max(needLevel, 1) * 2)) {
            repairAmount = repairItem.type.maxDurability / repairAmount * repairMultiple
            damageable.damage = min(repairItem.type.maxDurability - 1, damageable.damage + repairAmount)
            repairItem.itemMeta = damageable as ItemMeta
            player.sendActionBar('&', "&cFailed")
            player.playSound(block.location, Sound.BLOCK_ANVIL_DESTROY, 0.3f, 0.8f)
            return
        }
        
        repairAmount = repairItem.type.maxDurability / repairAmount * repairMultiple
        damageable.damage = max(0, damageable.damage - repairAmount)
        repairItem.itemMeta = damageable as ItemMeta
        player.playSound(block.location.add(.5, .0, .5), Sound.BLOCK_ANVIL_USE, 0.3f, 0.8f)
    }
}
