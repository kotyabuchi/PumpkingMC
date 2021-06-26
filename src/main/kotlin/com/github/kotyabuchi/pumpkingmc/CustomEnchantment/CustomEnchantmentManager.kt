package com.github.kotyabuchi.pumpkingmc.CustomEnchantment

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.getEquipmentType
import com.github.kotyabuchi.pumpkingmc.Utility.hasDurability
import com.github.kotyabuchi.pumpkingmc.Utility.toLore
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

object CustomEnchantmentManager: Listener {

    @EventHandler
    fun onEnchant(event: EnchantItemEvent) {
        val item = event.item
        CustomEnchantment.values().forEach { enchant ->
            if (item.type == Material.BOOK || enchant.canEnchantItem(item) && Random.nextInt(100) <= enchant.getProbability(event.expLevelCost)) {
                var conflict = false
                event.enchantsToAdd.keys.forEach {
                    if (enchant.conflictsWith(it)) conflict = true
                }
                if (!conflict) {
                    val level = min(enchant.maxLevel, Random.nextInt(enchant.startLevel, enchant.maxLevel + 1 + round(event.expLevelCost / 5.0).toInt()))
                    item.addCustomEnchant(enchant, level)
                }
            }
        }
    }

    @EventHandler
    fun onRemoveEnchant(event: PrepareResultEvent) {
        val inv = event.inventory as? GrindstoneInventory ?: return

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
            event.result = event.result?.removeCustomEnchant(it)
        }
    }

    @EventHandler
    fun onPrepare(event: PrepareAnvilEvent) {
        val inv = event.inventory
        var result = ItemExpansion(event.result ?: return)
        val item0 = inv.getItem(0) ?: return
        val item1 = inv.getItem(1)
        val meta0 = item0.itemMeta ?: return
        val meta1 = item1?.itemMeta

        val enchants0 = meta0.enchants
        val enchants1 = meta1?.enchants
        val customEnchants = mutableMapOf<CustomEnchantmentMaster, Int>()
        val increaseEnchants = mutableMapOf<Enchantment, Int>()

        enchants0.forEach { (enchant0, level0) ->
            if (enchant0 is CustomEnchantmentMaster) customEnchants[enchant0] = level0
            enchants1?.forEach { (enchant1, level1) ->
                if (enchant0.maxLevel > 1 && enchant0 == enchant1 && level0 == level1) increaseEnchants[enchant0] = level0 + 1
            }
        }
        meta1?.enchants?.forEach { (enchant, level) ->
            if (enchant is CustomEnchantmentMaster) customEnchants[enchant] = level
        }
        customEnchants.forEach { (enchant, level) ->
            result.addEnchant(enchant, level)
        }
        increaseEnchants.forEach { (enchant, level) ->
            result.setEnchantmentLevel(enchant, level)
        }
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

    private fun ItemStack.addCustomEnchant(enchant: CustomEnchantmentMaster, _level: Int) {
        val meta = this.itemMeta ?: return
        var level = _level
        if (meta.hasEnchant(enchant)) {
            level = max(meta.getEnchantLevel(enchant), level)
            this.removeEnchantment(enchant)
        }
        val lore = meta.lore ?: mutableListOf()

        lore.add(0, "&7${enchant.toLore(level)}".colorS())
        meta.lore = lore
        this.itemMeta = meta
        this.addUnsafeEnchantment(enchant, level)
    }

    private fun ItemStack.removeCustomEnchant(enchant: CustomEnchantmentMaster): ItemStack {
        val meta = this.itemMeta ?: return this
        val level = meta.getEnchantLevel(enchant)
        meta.removeEnchant(enchant)
        val lore = meta.lore ?: return this
        lore.remove("&7${enchant.toLore(level)}".colorS())
        meta.lore = lore
        this.itemMeta = meta
        return this
    }
}