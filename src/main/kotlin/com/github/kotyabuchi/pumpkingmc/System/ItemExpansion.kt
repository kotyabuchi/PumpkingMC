package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.Enum.ItemType
import com.github.kotyabuchi.pumpkingmc.Enum.MaterialMiningLevel
import com.github.kotyabuchi.pumpkingmc.Enum.Rarity
import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Utility.*
import de.tr7zw.nbtapi.NBTItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ItemExpansion {

    var item: ItemStack
    var displayName: Component?

    private val rarityKey = "ITEM_RARITY"
    private var rarity: Rarity

    private val itemTypeKey = "ITEM_TYPE"
    private val itemTypes: MutableList<ItemType> = mutableListOf()
    private val toolPartTypeKey = "TOOL_PART_TYPE"
    val toolPartType: ToolPartType?
    private val materialMiningLevelKey = "MATERIAL_MINING_LEVEL"
    val materialMiningLevel: MaterialMiningLevel?

    private val durabilityKey = "TOOL_DURABILITY"
    private val maxDurabilityKey = "TOOL_MAX_DURABILITY"
    private var hasDurability = false
    private var durability = 0
    private var maxDurability = 0

    constructor(type: Material, displayName: Component? = null, lore: MutableList<Component> = mutableListOf(), rarity: Rarity = Rarity.COMMON, itemTypes: List<ItemType> = listOf(), amount: Int = 1, toolPartType: ToolPartType? = null, materialMiningLevel: MaterialMiningLevel? = null) {
        item = ItemStack(type, amount)
        val meta = item.itemMeta
        meta.displayName(displayName)
        item.itemMeta = meta
        this.rarity = rarity
        this.displayName = displayName
        if (lore.isNotEmpty()) setLore(lore)
        if (itemTypes.isEmpty()) {
            if (type.isArmors()) this.itemTypes.add(ItemType.ARMOR)
            if (type.isTools()) this.itemTypes.add(ItemType.TOOL)
            if (type.isWeapons()) this.itemTypes.add(ItemType.WEAPON)
            if (type.isMaterial()) this.itemTypes.add(ItemType.MATERIAL)
        } else {
            this.itemTypes.addAll(itemTypes)
        }
        this.toolPartType = toolPartType
        this.materialMiningLevel = materialMiningLevel
        create()
    }

    constructor(item: ItemStack, displayName: Component? = item.itemMeta.displayName(), lore: MutableList<Component> = mutableListOf(), rarity: Rarity = Rarity.COMMON, itemTypes: List<ItemType> = listOf(), toolPartType: ToolPartType? = null, materialMiningLevel: MaterialMiningLevel? = null) {
        this.item = item
        val nbti = NBTItem(item)
        val type = item.type

        this.displayName = displayName

        val rarityStr = nbti.getString(rarityKey)
        this.rarity = if (rarityStr == null || rarityStr == "") rarity else Rarity.valueOf(rarityStr)

        if (lore.isNotEmpty()) setLore(lore)

        val itemTypesStr = nbti.getStringList(itemTypeKey)
        this.itemTypes.addAll(itemTypes)
        if ((itemTypesStr == null || itemTypesStr.isEmpty()) && itemTypes.isEmpty()) {
            if (type.isArmors()) this.itemTypes.add(ItemType.ARMOR)
            if (type.isTools()) this.itemTypes.add(ItemType.TOOL)
            if (type.isWeapons()) this.itemTypes.add(ItemType.WEAPON)
            if (type.isMaterial()) this.itemTypes.add(ItemType.MATERIAL)
        } else {
            itemTypesStr.forEach { itemTypeStr ->
                this.itemTypes.add(ItemType.valueOf(itemTypeStr))
            }
        }
        if (toolPartType == null && nbti.hasKey(toolPartTypeKey) && nbti.getObject(toolPartTypeKey, ToolPartType::class.java) != null) {
            this.toolPartType = nbti.getObject(toolPartTypeKey, ToolPartType::class.java) as ToolPartType
        } else {
            this.toolPartType = toolPartType
        }
        if (materialMiningLevel == null && nbti.hasKey(materialMiningLevelKey) && nbti.getObject(materialMiningLevelKey, MaterialMiningLevel::class.java) != null) {
            this.materialMiningLevel = nbti.getObject(materialMiningLevelKey, MaterialMiningLevel::class.java) as MaterialMiningLevel
        } else {
            this.materialMiningLevel = materialMiningLevel
        }

        this.item.itemMeta?.let { meta ->
            if (type.hasDurability()) {
                meta as Damageable
                if (nbti.hasKey(maxDurabilityKey)) {
                    val maxDurabilityInt = nbti.getInteger(maxDurabilityKey)
                    setMaxDurability(maxDurabilityInt)
                    val durabilityInt = nbti.getInteger(durabilityKey)
                    durabilityInt?.let {
                        setDurability(it)
                    }
                } else {
                    val maxDurability = round(type.maxDurability.toInt() * rarity.durabilityMultiple).toInt()
                    setMaxDurability(maxDurability)
                    setDurability(maxDurability - meta.damage)
                }
            }
        }
        create()
    }

    private fun create() {
        val nbti = NBTItem(item)
        nbti.setString(rarityKey, rarity.name)
        nbti.getStringList(itemTypeKey).clear()
        itemTypes.forEach {
            nbti.getStringList(itemTypeKey).add(it.name)
        }
        if (toolPartType == null) {
            nbti.removeKey(toolPartTypeKey)
        } else {
            nbti.setObject(toolPartTypeKey, toolPartType)
        }
        if (materialMiningLevel == null) {
            nbti.removeKey(materialMiningLevelKey)
        } else {
            nbti.setObject(materialMiningLevelKey, materialMiningLevel)
        }
        nbti.removeKey("TOOL_RARITY")
        item = nbti.item
        generateLore()
    }

    fun getRarity(): Rarity {
        return rarity
    }

    fun getItemTypes(): List<ItemType> {
        return itemTypes
    }

    fun removeItemType(itemType: ItemType): ItemExpansion {
        itemTypes.remove(itemType)
        create()
        return this
    }

    fun getDurability(): Int {
        return durability
    }

    fun getMaxDurability(): Int {
        return maxDurability
    }

    fun setDurability(durability: Int): ItemExpansion {
        if (!item.type.hasDurability()) return this
        val nbti = NBTItem(item)
        this.durability = min(durability, maxDurability)
        hasDurability = true
        nbti.setInteger(durabilityKey, durability)
        item = nbti.item
        val meta = item.itemMeta as Damageable
        val customPercent = durability.toDouble() / maxDurability
        val originalPercent = item.type.maxDurability * customPercent
        meta.damage = max(0, item.type.maxDurability - round(originalPercent).toInt())
        item.itemMeta = meta as ItemMeta
        generateLore()
        return this
    }

    fun setMaxDurability(durability: Int): ItemExpansion {
        if (!item.type.hasDurability()) return this
        val nbti = NBTItem(item)
        maxDurability = durability
        hasDurability = true
        nbti.setInteger(maxDurabilityKey, durability)
        item = nbti.item
        generateLore()
        return this
    }

    fun increaseDurability(amount: Int): ItemExpansion {
        if (!item.type.hasDurability()) return this
        setDurability(min(durability + amount, maxDurability))
        return this
    }

    fun reduceDurability(amount: Int): ItemExpansion {
        if (!item.type.hasDurability()) return this
        setDurability(max(0, min(durability - amount, maxDurability)))
        return this
    }

    fun isDurabilityDamaged(): Boolean {
        return (durability < maxDurability)
    }

    fun setDisplayName(displayName: Component): ItemExpansion {
        val meta = item.itemMeta ?: return this
        meta.displayName(displayName)
        item.itemMeta = meta
        return this
    }

    fun addEnchant(enchant: Enchantment, level: Int): ItemExpansion {
        if (item.containsEnchantment(enchant)) return this
        if (!enchant.canEnchantItem(item)) return this
        if (item.itemMeta.hasConflictingEnchant(enchant)) return this
        if (item.type == Material.ENCHANTED_BOOK && enchant !is CustomEnchantmentMaster) {
            item.editMeta {
                (it as? EnchantmentStorageMeta)?.addStoredEnchant(enchant, level, true)
            }
        } else {
            item.addUnsafeEnchantment(enchant, level)
        }
        generateLore()
        return this
    }

    fun removeEnchant(enchant: Enchantment): ItemExpansion {
        if (item.type == Material.ENCHANTED_BOOK && enchant !is CustomEnchantmentMaster) {
            item.editMeta {
                (it as? EnchantmentStorageMeta)?.removeStoredEnchant(enchant)
            }
        } else {
            item.removeEnchantment(enchant)
        }
        generateLore()
        return this
    }

    fun setEnchantmentLevel(enchant: Enchantment, level: Int): ItemExpansion {
        removeEnchant(enchant)
        addEnchant(enchant, level)
        return this
    }

    fun setFlag(flag: ItemFlag): ItemExpansion {
        val meta = item.itemMeta ?: return this
        if (!meta.hasItemFlag(flag)) meta.addItemFlags(flag)
        item.itemMeta = meta
        return this
    }

    fun getLore(): List<Component> {
        val result = mutableListOf<Component>()
        val nbti = NBTItem(item)
        val serializedLore = nbti.getStringList("lore")
        serializedLore.forEach {
            result.add(LegacyComponentSerializer.legacyAmpersand().deserialize(it))
        }
        return result
    }

    fun getLore(num: Int): Component? {
        val nbti = NBTItem(item)
        val lore = nbti.getStringList("lore")
        return if (lore.size > num) {
            LegacyComponentSerializer.legacyAmpersand().deserialize(lore[num])
        } else {
            null
        }
    }

    fun setLore(newLore: MutableList<Component>): ItemExpansion {
        val serializedLore = mutableListOf<String>()
        newLore.forEach {
            serializedLore.add(LegacyComponentSerializer.legacyAmpersand().serialize(it))
        }
        val nbti = NBTItem(item)
        nbti.setObject("lore", serializedLore)
        item = nbti.item
        return this
    }

    fun setLore(newLore: Component, num: Int): ItemExpansion {
        val lore = getLore().toMutableList()
        if (lore.size > num) {
            lore.removeAt(num)
        } else {
            repeat(num - lore.size + 1) {
                lore.add(Component.empty())
            }
        }
        lore[num] = newLore
        return this
    }

    fun addLore(vararg newLore: Component): ItemExpansion {
        val nbti = NBTItem(item)
        val serializedLore = nbti.getStringList("lore")
        newLore.forEach {
            serializedLore.add(LegacyComponentSerializer.legacyAmpersand().serialize(it))
        }
        nbti.setObject("lore", serializedLore)
        item = nbti.item
        return this
    }

    fun removeLore(vararg lore: Component): ItemExpansion {
        val nbti = NBTItem(item)
        val serializedLore = nbti.getStringList("lore")
        lore.forEach {
            serializedLore.remove(LegacyComponentSerializer.legacyAmpersand().serialize(it))
        }
        nbti.setObject("lore", serializedLore)
        item = nbti.item
        return this
    }

    fun setDummyEnchant(): ItemExpansion {
        return addEnchant(Enchantment.DURABILITY, 1).setFlag(ItemFlag.HIDE_ENCHANTS)
    }

    fun removeDummyEnchant(): ItemExpansion {
        return removeEnchant(Enchantment.DURABILITY)
    }

    fun generateLore(): List<Component> {
        val meta = item.itemMeta
        val lore = mutableListOf<Component>()

        meta.displayName(this.displayName)

        meta?.enchants?.forEach { (enchant, level) ->
            if (enchant is CustomEnchantmentMaster) {
                lore.add(Component.text(enchant.toLore(level), NamedTextColor.GRAY).normal())
            }
        }

        lore.add(Component.text("Rarity ", NamedTextColor.GREEN, TextDecoration.BOLD).normal()
            .append(Component.text("> ", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
            .append(Component.text(rarity.name, rarity.color).normal(TextDecoration.BOLD)))
        if (materialMiningLevel != null) {
            lore.add(Component.text("MiningLevel ", NamedTextColor.DARK_AQUA, TextDecoration.BOLD).normal()
                .append(Component.text("> ", NamedTextColor.DARK_BLUE))
                .append(Component.text(materialMiningLevel.getRegularName(), NamedTextColor.WHITE).normal(TextDecoration.BOLD)))
        }

        if (hasDurability) {
            lore.add(Component.empty())
            lore.add(Component.text("Durability ", NamedTextColor.AQUA, TextDecoration.BOLD).normal()
                .append(Component.text("> ", NamedTextColor.BLUE))
                .append(Component.text("$durability / ", NamedTextColor.WHITE).normal(TextDecoration.BOLD))
                .append(Component.text(maxDurability, NamedTextColor.WHITE, TextDecoration.BOLD)))
        }

        if (itemTypes.isNotEmpty()) {
            var itemTypeLore = Component.empty()
            itemTypes.forEachIndexed { index, itemType ->
                if (index > 0) itemTypeLore = itemTypeLore.append(Component.text(", ", NamedTextColor.WHITE).normal())
                itemTypeLore = itemTypeLore.append(Component.text(itemType.getRegularName(), NamedTextColor.WHITE).normal(TextDecoration.BOLD))
            }
            lore.add(Component.empty())
            lore.add(Component.text("ItemType ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD).normal()
                .append(Component.text("> ", NamedTextColor.DARK_PURPLE))
                .append(itemTypeLore))
        }

        if (toolPartType != null) {
            lore.add(Component.text("ToolPartType ", NamedTextColor.YELLOW, TextDecoration.BOLD).normal()
                .append(Component.text("> ", NamedTextColor.GOLD).normal())
                .append(Component.text(toolPartType.getRegularName(), NamedTextColor.WHITE).normal(TextDecoration.BOLD)))
        }

        val itemLore = getLore()
        if (itemLore.isNotEmpty()) {
            lore.add(Component.text("---------------", NamedTextColor.GRAY).normal(TextDecoration.BOLD))
            lore.addAll(itemLore)
        }

        meta?.lore(lore)
        item.itemMeta = meta
        return lore
    }
}