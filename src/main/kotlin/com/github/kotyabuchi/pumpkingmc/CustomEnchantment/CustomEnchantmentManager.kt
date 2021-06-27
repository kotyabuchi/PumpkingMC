package com.github.kotyabuchi.pumpkingmc.CustomEnchantment

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.getEquipmentType
import com.github.kotyabuchi.pumpkingmc.Utility.hasDurability
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

object CustomEnchantmentManager: Listener {

    @EventHandler
    fun onEnchant(event: EnchantItemEvent) {
        val item = event.item
        val toAdd = event.enchantsToAdd
        CustomEnchantment.values().forEach { enchant ->
            val random = Random.nextInt(100)
            if ((item.type == Material.BOOK || enchant.canEnchantItem(item)) && random <= enchant.getProbability(event.expLevelCost)) {
                var conflict = false
                toAdd.keys.forEach {
                    if (enchant.conflictsWith(it)) conflict = true
                }
                if (!conflict) {
                    val level = min(enchant.maxLevel, Random.nextInt(enchant.startLevel, enchant.maxLevel + 1 + round(event.expLevelCost / 5.0).toInt()))
                    toAdd[enchant] = level
                }
            }
        }
        object : BukkitRunnable() {
            override fun run() {
                val result: ItemExpansion =
                    if (item.type == Material.BOOK) {
                        ItemExpansion(Material.ENCHANTED_BOOK)
                    } else {
                        ItemExpansion(item)
                    }
                toAdd.forEach { (enchant, level) ->
                    result.addEnchant(enchant, level)
                }
                event.inventory.setItem(0, result.item)
            }
        }.runTaskLater(instance, 0)
    }

    @EventHandler
    fun onRemoveEnchant(event: PrepareResultEvent) {
        val inv = event.inventory as? GrindstoneInventory ?: return
        val result = event.result?.let { ItemExpansion(it) } ?: return

        val item0 = inv.getItem(0)
        val item1 = inv.getItem(1)

        val meta0 = item0?.itemMeta
        val meta1 = item1?.itemMeta

        val customEnchants = mutableListOf<CustomEnchantmentMaster>()
        meta0?.enchants?.keys?.forEach {
            if (it is CustomEnchantmentMaster) customEnchants.add(it)
        }
        meta1?.enchants?.keys?.forEach {
            if (it is CustomEnchantmentMaster) customEnchants.add(it)
        }
        customEnchants.forEach {
            result.removeEnchant(it)
        }
        event.result = result.item
    }

    @EventHandler
    fun onPrepare(event: PrepareAnvilEvent) {
        val inv = event.inventory
        var result = ItemExpansion(event.result ?: return)
        val item0 = inv.getItem(0) ?: return
        val item1 = inv.getItem(1)
        val meta0 = item0.itemMeta ?: return
        val meta1 = item1?.itemMeta

        val enchants0 = mutableMapOf<Enchantment, Int>()
        val enchants1 = mutableMapOf<Enchantment, Int>()
        enchants0.putAll(meta0.enchants)
        meta1?.enchants?.let {
            enchants1.putAll(it)
        }
        if (item0.type == Material.ENCHANTED_BOOK) {
            enchants0.putAll((meta0 as EnchantmentStorageMeta).storedEnchants)
        }
        if (item1?.type == Material.ENCHANTED_BOOK) {
            enchants1.putAll((meta1 as EnchantmentStorageMeta).storedEnchants)
        }

        val customEnchants = mutableMapOf<CustomEnchantmentMaster, Int>()
        val increaseEnchants = mutableMapOf<Enchantment, Int>()

        enchants0.forEach { (enchant0, level0) ->
            if (enchant0 is CustomEnchantmentMaster) customEnchants[enchant0] = level0
            enchants1.forEach { (enchant1, level1) ->
                if (enchant0.maxLevel > 1 && enchant0 == enchant1 && level0 == level1) increaseEnchants[enchant0] = level0 + 1
            }
        }
        enchants1.forEach { (enchant, level) ->
            if (enchant is CustomEnchantmentMaster) {
                if (customEnchants.containsKey(enchant)) {
                    if (customEnchants[enchant]!! < level) customEnchants[enchant] = level
                } else {
                    customEnchants[enchant] = level
                }
            }
        }

        if (result.item.type == Material.ENCHANTED_BOOK) {
            result.item.editMeta { meta ->
                (meta as? EnchantmentStorageMeta)?.let {
                    customEnchants.forEach { (enchant, level) ->
                        if (!meta.hasConflictingStoredEnchant(enchant)) meta.addStoredEnchant(enchant, level, true)
                    }
                }
            }
        } else {
            customEnchants.forEach { (enchant, level) ->
                result.addEnchant(enchant, level)
            }
            increaseEnchants.forEach { (enchant, level) ->
                result.setEnchantmentLevel(enchant, level)
            }
        }

        // mending durability
        if (item1 != null && item0.type.hasDurability()) {
            if (item0.type == item1.type) {
                result = result.increaseDurability(ItemExpansion(item1).getDurability())
            } else if (item0.type == result.item.type) {
                val durability0 = meta0 as? Damageable
                val resultDurability = event.result?.itemMeta as? Damageable
                if (durability0 != null && resultDurability != null) {
                    if (resultDurability.damage == 0) {
                        result.setDurability(result.getMaxDurability())
                    } else {
                        val equipmentType = item0.type.getEquipmentType()
                        val cost = equipmentType?.materialCost
                        if (equipmentType != null && cost != null) {
                            val increaseAmount = ceil(result.getMaxDurability() / cost.toDouble()).toInt() * item1.amount
                            result.increaseDurability(increaseAmount)
                        }
                    }
                }
            }
        }

        event.result = result.item
    }
}