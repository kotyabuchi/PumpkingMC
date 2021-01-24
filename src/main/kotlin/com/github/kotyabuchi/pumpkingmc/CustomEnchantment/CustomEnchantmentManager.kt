package com.github.kotyabuchi.pumpkingmc.CustomEnchantment

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.hasDurability
import com.github.kotyabuchi.pumpkingmc.Utility.toLore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

class CustomEnchantmentManager: Listener {

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

        val increasedDurability = ((meta0 as? Damageable)?.damage ?: 0) - ((event.result?.itemMeta as? Damageable)?.damage ?: 0)
        result.increaseDurability(increasedDurability)

        val customEnchants = mutableMapOf<CustomEnchantmentMaster, Int>()
        meta0.enchants.forEach { (enchant, level) ->
            if (enchant is CustomEnchantmentMaster) customEnchants[enchant] = level
        }
        meta1?.enchants?.forEach { (enchant, level) ->
            if (enchant is CustomEnchantmentMaster) customEnchants[enchant] = level
        }
        customEnchants.forEach { (enchant, level) ->
            result.addEnchant(enchant, level)
        }
        if (item1 != null && item0.type.hasDurability() && item0.type == item1.type) {
            result = result.increaseDurability(ItemExpansion(item1).getDurability())
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