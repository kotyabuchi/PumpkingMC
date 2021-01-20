package com.github.kotyabuchi.pumpkingmc.CustomEnchantment

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

open class CustomEnchantmentMaster(private val name: String): Enchantment(NamespacedKey(instance, "ENCHANTMENT_${name.replace(" ", "_")}")), Listener {

    val enchantKey = NamespacedKey(instance, name.replace(" ", "_"))

    init {
        instance.server.pluginManager.registerEvents(this, instance)
    }

    open fun getProbability(expCost: Int): Int {
        return 0
    }

    override fun getName(): String {
        return name
    }

    override fun getMaxLevel(): Int {
        return 1
    }

    override fun getStartLevel(): Int {
        return 1
    }

    override fun getItemTarget(): EnchantmentTarget {
        return EnchantmentTarget.BREAKABLE
    }

    override fun isTreasure(): Boolean {
        return false
    }

    override fun isCursed(): Boolean {
        return false
    }

    override fun conflictsWith(other: Enchantment): Boolean {
        return false
    }

    override fun canEnchantItem(item: ItemStack): Boolean {
        return true
    }
}