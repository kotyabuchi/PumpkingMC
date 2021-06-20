package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.Enum.ArmorType
import com.github.kotyabuchi.pumpkingmc.Enum.EquipmentType
import com.github.kotyabuchi.pumpkingmc.Enum.MaterialMiningLevel
import com.github.kotyabuchi.pumpkingmc.Enum.ToolType
import com.github.kotyabuchi.pumpkingmc.instance
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*
import kotlin.random.Random

fun Material.hasDurability(): Boolean {
    return this.isTools() || this.isWeapons() || this.isArmors() || this.isShield()
}

fun Material.isSword(): Boolean {
    return ToolType.SWORD.includes(this)
}

fun Material.isPickAxe(): Boolean {
    return ToolType.PICKAXE.includes(this)
}

fun Material.isAxe(): Boolean {
    return ToolType.AXE.includes(this)
}

fun Material.isShovel(): Boolean {
    return ToolType.SHOVEL.includes(this)
}

fun Material.isHoe(): Boolean {
    return ToolType.HOE.includes(this)
}

fun Material.isShield(): Boolean {
    return ToolType.SHIELD.includes(this)
}

fun Material.isTools(): Boolean {
    return this == Material.SHIELD ||
            this.isPickAxe() ||
            this.isShovel() ||
            this.isHoe() ||
            this.isAxe() ||
            this == Material.FISHING_ROD ||
            this == Material.FLINT_AND_STEEL
}

fun Material.isWeapons(): Boolean {
    return this == Material.BOW ||
            this == Material.CROSSBOW ||
            this.isSword() ||
            this.isAxe()
}

fun Material.isHelmet(): Boolean {
    return ArmorType.HELMET.includes(this)
}

fun Material.isChestplate(): Boolean {
    return ArmorType.CHESTPLATE.includes(this)
}

fun Material.isLeggings(): Boolean {
    return ArmorType.LEGGINGS.includes(this)
}

fun Material.isBoots(): Boolean {
    return ArmorType.BOOTS.includes(this)
}

fun Material.isArmors(): Boolean {
    return this.isHelmet() ||
            this.isChestplate() ||
            this.isLeggings() ||
            this.isBoots()
}

fun Material.getEquipmentType(): EquipmentType? {
    ToolType.values().forEach {
        if (it.includes(this)) return it
    }
    ArmorType.values().forEach {
        if (it.includes(this)) return it
    }
    return null
}

fun Material.isMaterial(): Boolean {
    return this == Material.COBBLESTONE ||
            this == Material.IRON_INGOT ||
            this == Material.GOLD_INGOT ||
            this == Material.DIAMOND ||
            this == Material.EMERALD ||
            this == Material.NETHERITE_INGOT ||
            this == Material.STRING ||
            this == Material.PAPER ||
            this == Material.BONE ||
            this == Material.BLAZE_ROD ||
            this == Material.NETHER_STAR ||
            this == Material.SCUTE ||
            this == Material.SLIME_BLOCK ||
            this == Material.OBSIDIAN ||
            this == Material.BLUE_ICE ||
            this == Material.PHANTOM_MEMBRANE ||
            this == Material.OAK_PLANKS ||
            this == Material.BIRCH_PLANKS ||
            this == Material.SPRUCE_PLANKS ||
            this == Material.ACACIA_PLANKS ||
            this == Material.DARK_OAK_PLANKS ||
            this == Material.JUNGLE_PLANKS ||
            this == Material.CRIMSON_PLANKS ||
            this == Material.WARPED_PLANKS
}

fun Material.canUseToolHead(): Boolean {
    return this == Material.COBBLESTONE ||
            this == Material.IRON_INGOT ||
            this == Material.GOLD_INGOT ||
            this == Material.DIAMOND ||
            this == Material.EMERALD ||
            this == Material.NETHERITE_INGOT ||
            this == Material.NETHER_STAR ||
            this == Material.OBSIDIAN ||
            this == Material.BLUE_ICE ||
            this == Material.OAK_PLANKS ||
            this == Material.BIRCH_PLANKS ||
            this == Material.SPRUCE_PLANKS ||
            this == Material.ACACIA_PLANKS ||
            this == Material.DARK_OAK_PLANKS ||
            this == Material.JUNGLE_PLANKS ||
            this == Material.CRIMSON_PLANKS ||
            this == Material.WARPED_PLANKS
}

fun Material.getMiningLevel(): MaterialMiningLevel? {
    return when(this) {
        Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.SPRUCE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS, Material.JUNGLE_PLANKS, Material.CRIMSON_PLANKS, Material.WARPED_PLANKS -> MaterialMiningLevel.WOOD
        Material.COBBLESTONE -> MaterialMiningLevel.STONE
        Material.IRON_INGOT, Material.BLUE_ICE, Material.OBSIDIAN -> MaterialMiningLevel.IRON
        Material.GOLD_INGOT -> MaterialMiningLevel.GOLDEN
        Material.DIAMOND, Material.EMERALD -> MaterialMiningLevel.DIAMOND
        Material.NETHERITE_INGOT, Material.NETHER_STAR -> MaterialMiningLevel.NETHERITE
        else -> null
    }
}

fun ItemStack.damage(player: Player, _amount: Int) {
    if (this.itemMeta is Damageable) {
        var amount = _amount
        val damageChance = 100 / (this.getEnchantmentLevel(Enchantment.DURABILITY) + 1)
        if (this.containsEnchantment(Enchantment.DURABILITY)) {
            repeat(_amount) {
                if (Random.nextInt(100) <= damageChance) amount--
            }
        }
        if (amount > 0) instance.server.pluginManager.callEvent(PlayerItemDamageEvent(player, this, amount))
    }
}

fun ItemStack.toSerializedString(): String {
    return Base64.getEncoder().encodeToString(this.serializeAsBytes())
}

object ItemUtil {
    fun deserializeItem(serializeString: String): ItemStack {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(serializeString))
    }
}

fun createHead(name: String, ownerName: String, uuid: String, textureData: String): ItemStack {
    val head = ItemStack(Material.PLAYER_HEAD)
    val nbti = NBTItem(head)

    val disp = nbti.addCompound("display")
    disp.setString("Name", name)

    val skull = nbti.addCompound("SkullOwner")
    skull.setString("Name", ownerName)
    skull.setString("Id", uuid)

    val texture = skull.addCompound("Properties").getCompoundList("textures").addCompound()
    texture.setString("Value", textureData)

    return nbti.item
}