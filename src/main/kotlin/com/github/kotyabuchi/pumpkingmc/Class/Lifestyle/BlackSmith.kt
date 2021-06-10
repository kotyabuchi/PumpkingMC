package com.github.kotyabuchi.pumpkingmc.Class.Lifestyle

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round
import kotlin.random.Random

object BlackSmith: JobClassMaster("BLACKSMITH") {

    private val key = NamespacedKey(instance, "FURNACE_HOLDER")
    private val expMap = mutableMapOf<Material, Float>()


    init {
        val server = instance.server
        val removeRecipes = mutableListOf<NamespacedKey>()
        server.recipeIterator().forEach {
            if (it is FurnaceRecipe) {
                expMap[it.input.type] = it.experience
            }

            val result = it.result.type.name
            if (result.endsWith("_SWORD") ||
                    result.endsWith("_BOW") ||
                    result.endsWith("_AXE") ||
                    result.endsWith("_PICKAXE") ||
                    result.endsWith("_SHOVEL") ||
                    result.endsWith("_HOE") ||
                    result.endsWith("_HELMET") ||
                    result.endsWith("_CHESTPLATE") ||
                    result.endsWith("_LEGGINGS") ||
                    result.endsWith("_BOOTS")) {
                if (it is Keyed) removeRecipes.add(it.key)
            }
        }
//        removeRecipes.forEach {
//            server.removeRecipe(it)
//        }
    }

//    @EventHandler
//    fun onPrepareCraft(event: PrepareItemCraftEvent) {
//        event.inventory.result?.let {
//            event.inventory.result = ItemExpansion(it).item
//        }
//    }
//
//    @EventHandler
//    fun onOpenSmithyMenu(event: PlayerInteractEvent) {
//        if (event.isCancelled) return
//        if (event.action != Action.RIGHT_CLICK_BLOCK) return
//        val block = event.clickedBlock ?: return
//        val playerStatus = event.player.getStatus()
//
//        if (block.type == Material.SMITHING_TABLE) {
//            event.isCancelled = true
//            playerStatus.openMenu(SmithingPatternMenu())
//        } else if (block.type == Material.ANVIL) {
//            event.isCancelled = true
//            playerStatus.openMenu(ToolStationMenu())
//        }
//    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val player = event.player
        val block = event.block

        if (block.type != Material.FURNACE && block.type != Material.BLAST_FURNACE) return
        val state = block.state as? Furnace ?: return
        val pdc = state.persistentDataContainer
        pdc.set(key, PersistentDataType.STRING, player.uniqueId.toString())
        state.update()
    }

    @EventHandler
    fun onItemMoveToFurnace(event: InventoryMoveItemEvent) {
        val inv = event.destination as? FurnaceInventory ?: return
        val block = inv.location?.block ?: return
        object : BukkitRunnable() {
            override fun run() {
                val state = block.state as? Furnace ?: return
                if (state.cookTime.toInt() != 0) return
                val pdc = state.persistentDataContainer
                if (!pdc.has(key, PersistentDataType.STRING)) return
                val player = instance.server.getPlayer(UUID.fromString(pdc.getOrDefault(key, PersistentDataType.STRING, ""))) ?: return
                if (player.isOnline) {
                    val level = player.getStatus().getJobClassStatus(this@BlackSmith).getLevel()
                    state.cookTimeTotal = getSmeltingEfficiency(level, state.cookTimeTotal)
                    state.update()
                }
            }
        }.runTaskLater(instance, 0)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inv = event.clickedInventory ?: return
        val furnaceInv = event.inventory as? FurnaceInventory ?: return
        val block = furnaceInv.location?.block ?: return

        if ((inv == furnaceInv && !event.isShiftClick) || (inv is PlayerInventory && event.isShiftClick)) {
            object : BukkitRunnable() {
                override fun run() {
                    val state = block.state as? Furnace ?: return
                    if (state.cookTime.toInt() != 0) return
                    val pdc = state.persistentDataContainer
                    if (!pdc.has(key, PersistentDataType.STRING)) return
                    val player = instance.server.getPlayer(UUID.fromString(pdc.getOrDefault(key, PersistentDataType.STRING, ""))) ?: return
                    if (player.isOnline) {
                        val level = player.getStatus().getJobClassStatus(this@BlackSmith).getLevel()
                        state.cookTimeTotal = getSmeltingEfficiency(level, state.cookTimeTotal)
                        state.update()
                    }
                }
            }.runTaskLater(instance, 0)
        }
    }

    @EventHandler
    fun onStartSmelt(event: FurnaceBurnEvent) {
        val block = event.block
        val state = block.state as? Furnace ?: return
        val pdc = state.persistentDataContainer
        if (!pdc.has(key, PersistentDataType.STRING)) return
        val player = instance.server.getPlayer(UUID.fromString(pdc.getOrDefault(key, PersistentDataType.STRING, ""))) ?: return
        if (player.isOnline) {
            val level = player.getStatus().getJobClassStatus(this).getLevel()
            event.burnTime = getFuelEfficiency(level, event.burnTime)
            object : BukkitRunnable() {
                override fun run() {
                    val writeState = block.state as? Furnace ?: return
                    writeState.cookTimeTotal = getSmeltingEfficiency(level, writeState.cookTimeTotal + 1)
                    writeState.update()
                }
            }.runTaskLater(instance, 0)
        }
    }

    @EventHandler
    fun onSmelt(event: FurnaceSmeltEvent) {
        if (!event.result.type.isBlock) event.result = ItemExpansion(event.result).item

        val block = event.block
        val blockState = block.state as? Furnace ?: return
        val pdc = blockState.persistentDataContainer
        if (!pdc.has(key, PersistentDataType.STRING)) return
        val player = instance.server.getPlayer(UUID.fromString(pdc.getOrDefault(key, PersistentDataType.STRING, ""))) ?: return
        if (!player.isOnline) return
        val status = player.getStatus()
        val level = status.getJobClassStatus(this).getLevel()

        val multipleSmeltChange = level * 0.4
        var smeltAmount = 1 + floor(multipleSmeltChange / 100.0).toInt()
        if (Random.nextInt(100) < multipleSmeltChange % 100) smeltAmount++
        event.result.amount = smeltAmount

        val exp = expMap[event.source.type] ?: 0.1
        status.addSkillExp(this, exp.toDouble() * 15 * smeltAmount)

        object : BukkitRunnable() {
            override fun run() {
                val state = block.state as? Furnace ?: return
                state.cookTimeTotal = getSmeltingEfficiency(level, state.cookTimeTotal)
                state.burnTime--
                state.update()
            }
        }.runTaskLater(instance, 0)
    }

    private fun getFuelEfficiency(level: Int, burnTime: Int): Int {
        return round((1 + (level / 200.0)) * burnTime).toInt()
    }

    private fun getSmeltingEfficiency(level: Int, cookTime: Int): Int {
        return max(1, cookTime - floor(level / 5.0).toInt())
    }
}