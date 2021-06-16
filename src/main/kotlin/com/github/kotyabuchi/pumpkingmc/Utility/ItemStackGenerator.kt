package com.github.kotyabuchi.pumpkingmc.Utility

import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemStackGenerator {
    private var result: ItemStack
    private var displayName: String? = null
    private var lore: List<String>? = null

    constructor(type: Material, amount: Int = 1) {
        this.result = ItemStack(type, amount)
    }

    constructor(item: ItemStack) {
        this.result = item
        val meta = item.itemMeta
        lore = meta?.lore
    }

    fun setDisplayName(displayName: String): ItemStackGenerator {
        this.displayName = "&f${displayName}".colorS()
        return this
    }

    fun setAmount(amount: Int): ItemStackGenerator {
        result.amount = amount
        return this
    }

    fun setLore(lore: List<String>): ItemStackGenerator {
        val resultLore = mutableListOf<String>()
        lore.forEach {
            resultLore.add("&f${it}".colorS())
        }
        this.lore = resultLore
        return this
    }

    fun setFlag(flag: ItemFlag): ItemStackGenerator {
        val meta = result.itemMeta ?: return this
        meta.addItemFlags(flag)
        result.itemMeta = meta
        return this
    }

    fun setMenuItemTag(): ItemStackGenerator {
        val nbti = NBTItem(result)
        nbti.setBoolean("IS_MENU_ITEM", true)
        result = nbti.item
        setFlag(ItemFlag.HIDE_ATTRIBUTES)
        return this
    }

    fun addEnchantment(enchant: Enchantment, level: Int): ItemStackGenerator {
        val meta = result.itemMeta ?: return this
        meta.addEnchant(enchant, level, true)
        result.itemMeta = meta
        return this
    }

    fun addFakeEnchantment(): ItemStackGenerator {
        setFlag(ItemFlag.HIDE_ENCHANTS)
        addEnchantment(Enchantment.DURABILITY, 1)
        return this
    }

    fun generate(): ItemStack {
        result.itemMeta?.let {
            it.setDisplayName(displayName)
            it.lore = lore
            result.itemMeta = it
        }
        return result
    }
}