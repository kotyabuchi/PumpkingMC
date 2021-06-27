package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.round

object SoulBind: CustomEnchantmentMaster("SOUL_BIND") {

    private val itemTarget = EnchantmentTarget.VANISHABLE

    override fun getMaxLevel(): Int {
        return 1
    }

    override fun getItemTarget(): EnchantmentTarget {
        return itemTarget
    }

    override fun canEnchantItem(item: ItemStack): Boolean {
        return true
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val dropItem = event.drops

        dropItem.forEach { item ->
            item.itemMeta?.let { itemMeta ->
                if (itemMeta.hasEnchant(this)) event.itemsToKeep.add(item)
            }
        }
    }
}