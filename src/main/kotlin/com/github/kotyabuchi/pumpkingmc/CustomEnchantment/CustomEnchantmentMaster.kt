package com.github.kotyabuchi.pumpkingmc.CustomEnchantment

import com.github.kotyabuchi.pumpkingmc.Utility.toRomanNumeral
import com.github.kotyabuchi.pumpkingmc.instance
import io.papermc.paper.enchantments.EnchantmentRarity
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityCategory
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

abstract class CustomEnchantmentMaster(
    private val name: String,
    enchantKey: NamespacedKey = NamespacedKey(instance, "ENCHANTMENT_${name.replace(" ", "_")}"))
    : Enchantment(enchantKey), Listener {

    init {
        instance.server.pluginManager.registerEvents(this, instance)
    }

    open fun getProbability(expCost: Int): Int {
        return expCost * rarity.weight * 2 / 10
    }

    final override fun getName(): String {
        return name
    }

    override fun getStartLevel(): Int {
        return 1
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
        return itemTarget.includes(item)
    }

    override fun displayName(level: Int): Component {
        return Component.text("$name ${level.toRomanNumeral()}")
    }

    override fun isTradeable(): Boolean {
        return false
    }

    override fun isDiscoverable(): Boolean {
        return true
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.COMMON
    }

    override fun getDamageIncrease(level: Int, entityCategory: EntityCategory): Float {
        return 0f
    }

    override fun getActiveSlots(): MutableSet<EquipmentSlot> {
        return mutableSetOf()
    }
}