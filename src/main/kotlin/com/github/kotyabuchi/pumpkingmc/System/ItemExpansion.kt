package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.Enum.ItemType
import com.github.kotyabuchi.pumpkingmc.Enum.MaterialMiningLevel
import com.github.kotyabuchi.pumpkingmc.Enum.Rarity
import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Utility.*
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ItemExpansion {

    var item: ItemStack
    var displayName: String?
    val itemLore: MutableList<String>

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

    constructor(type: Material, displayName: String?, lore: MutableList<String> = mutableListOf(), rarity: Rarity = Rarity.COMMON, itemTypes: List<ItemType> = listOf(), amount: Int = 1, toolPartType: ToolPartType? = null, materialMiningLevel: MaterialMiningLevel? = null) {
        this.rarity = rarity
        this.displayName = displayName
        this.itemLore = lore
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
        item = ItemStack(type, amount)
        val meta = item.itemMeta
        meta.setDisplayName(displayName)
        item.itemMeta = meta
        create()
    }

    constructor(item: ItemStack, displayName: String? = item.itemMeta.displayName, lore: MutableList<String> = item.itemMeta.lore ?: mutableListOf(), rarity: Rarity = Rarity.COMMON, itemTypes: List<ItemType> = listOf(), toolPartType: ToolPartType? = null, materialMiningLevel: MaterialMiningLevel? = null) {
        this.item = item
        val nbti = NBTItem(item)
        val type = item.type
        val meta = item.itemMeta

        this.displayName = displayName

        val rarityStr = nbti.getString(rarityKey)
        this.rarity = if (rarityStr == null || rarityStr == "") rarity else Rarity.valueOf(rarityStr)

        if (lore.isNotEmpty()) {
            if (lore.contains("&a&lRarity&r &2> ${this.rarity.text}".colorS())) {
                if (lore.contains("&7---------------".colorS())) {
                    val index = lore.indexOf("&7---------------".colorS())
                    lore.removeAll(lore.subList(0, index + 1))
                } else {
                    lore.clear()
                }
            }
        }
        this.itemLore = lore

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

        meta?.let {
            if (type.hasDurability()) {
                it as Damageable
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
                    setDurability(maxDurability - it.damage)
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

    fun setDisplayName(displayName: String): ItemExpansion {
        val meta = item.itemMeta ?: return this
        meta.setDisplayName(displayName.colorS())
        item.itemMeta = meta
        return this
    }

    fun addEnchant(enchant: Enchantment, level: Int): ItemExpansion {
        if (item.containsEnchantment(enchant)) return this
        item.addUnsafeEnchantment(enchant, level)
        generateLore()
        return this
    }

    fun removeEnchant(enchant: Enchantment): ItemExpansion {
        item.removeEnchantment(enchant)
        generateLore()
        return this
    }

    fun setFlag(flag: ItemFlag): ItemExpansion {
        val meta = item.itemMeta ?: return this
        if (!meta.hasItemFlag(flag)) meta.addItemFlags(flag)
        item.itemMeta = meta
        return this
    }

    fun setDummyEnchant(): ItemExpansion {
        return addEnchant(Enchantment.DURABILITY, 1).setFlag(ItemFlag.HIDE_ENCHANTS)
    }

    fun removeDummyEnchant(): ItemExpansion {
        return removeEnchant(Enchantment.DURABILITY)
    }

    fun generateLore(): List<String> {
        val meta = item.itemMeta
        val lore = mutableListOf<String>()

        meta.setDisplayName(this.displayName)

        meta?.enchants?.forEach { enchant, level ->
            if (enchant is CustomEnchantmentMaster) {
                lore.add("&7${enchant.toLore(level)}".colorS())
            }
        }

        lore.add("&a&lRarity&r &2> ${rarity.text}".colorS())
        if (materialMiningLevel != null) {
            lore.add("&3&lMiningLevel&r &1> &f${materialMiningLevel.getRegularName()}".colorS())
        }

        if (hasDurability) {
            lore.add("")
            lore.add("&b&lDurability&r &9> &f${durability} / &l${maxDurability}".colorS())
        }

        if (itemTypes.isNotEmpty()) {
            var itemTypeLore = "&d&lItemType&r &5> "
            itemTypes.forEachIndexed { index, itemType ->
                if (index > 0) itemTypeLore += ", "
                itemTypeLore += "&f${itemType.getRegularName()}"
            }
            lore.add("")
            lore.add(itemTypeLore.colorS())
        }

        if (toolPartType != null) {
            lore.add("&e&lToolPartType&r &6> &f${toolPartType.getRegularName()}".colorS())
        }

        if (itemLore.isNotEmpty()) {
            lore.add("&7---------------".colorS())
            lore.add("")
            lore.addAll(itemLore)
        }

        meta?.lore = lore
        item.itemMeta = meta
        return lore
    }
}