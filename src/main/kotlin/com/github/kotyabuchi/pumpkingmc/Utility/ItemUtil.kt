package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.Enum.MaterialMiningLevel
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Material.hasDurability(): Boolean {
    return this.isTools() || this.isWeapon() || this.isArmors()
}

fun Material.isTools(): Boolean {
    return this == Material.SHIELD ||
            this == Material.WOODEN_HOE ||
            this == Material.WOODEN_PICKAXE ||
            this == Material.WOODEN_SHOVEL ||
            this == Material.STONE_HOE ||
            this == Material.STONE_PICKAXE ||
            this == Material.STONE_SHOVEL ||
            this == Material.IRON_HOE ||
            this == Material.IRON_PICKAXE ||
            this == Material.IRON_SHOVEL ||
            this == Material.GOLDEN_HOE ||
            this == Material.GOLDEN_PICKAXE ||
            this == Material.GOLDEN_SHOVEL ||
            this == Material.DIAMOND_HOE ||
            this == Material.DIAMOND_PICKAXE ||
            this == Material.DIAMOND_SHOVEL ||
            this == Material.NETHERITE_HOE ||
            this == Material.NETHERITE_PICKAXE ||
            this == Material.NETHERITE_SHOVEL ||
            this == Material.NETHERITE_SWORD ||
            this == Material.FISHING_ROD ||
            this == Material.FLINT_AND_STEEL
}

fun Material.isWeapon(): Boolean {
    return this == Material.BOW ||
            this == Material.CROSSBOW ||
            this == Material.WOODEN_AXE ||
            this == Material.WOODEN_SWORD ||
            this == Material.STONE_AXE ||
            this == Material.STONE_SWORD ||
            this == Material.IRON_AXE ||
            this == Material.IRON_SWORD ||
            this == Material.GOLDEN_AXE ||
            this == Material.GOLDEN_SWORD ||
            this == Material.DIAMOND_AXE ||
            this == Material.DIAMOND_SWORD ||
            this == Material.NETHERITE_AXE ||
            this == Material.NETHERITE_SWORD
}

fun Material.isArmors(): Boolean {
    return this == Material.LEATHER_HELMET ||
            this == Material.LEATHER_CHESTPLATE ||
            this == Material.LEATHER_LEGGINGS ||
            this == Material.LEATHER_BOOTS ||
            this == Material.IRON_HELMET ||
            this == Material.IRON_CHESTPLATE ||
            this == Material.IRON_LEGGINGS ||
            this == Material.IRON_BOOTS ||
            this == Material.GOLDEN_HELMET ||
            this == Material.GOLDEN_CHESTPLATE ||
            this == Material.GOLDEN_LEGGINGS ||
            this == Material.GOLDEN_BOOTS ||
            this == Material.DIAMOND_HELMET ||
            this == Material.DIAMOND_CHESTPLATE ||
            this == Material.DIAMOND_LEGGINGS ||
            this == Material.DIAMOND_BOOTS ||
            this == Material.NETHERITE_HELMET ||
            this == Material.NETHERITE_CHESTPLATE ||
            this == Material.NETHERITE_LEGGINGS ||
            this == Material.NETHERITE_BOOTS ||
            this == Material.TURTLE_HELMET ||
            this == Material.SHIELD
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

fun createHead(name: String, lore: List<String>, ownerName: String, uuid: String, textureData: String): ItemStack {
    val head = ItemStack(Material.PLAYER_HEAD)
    val nbti = NBTItem(head)

    val disp = nbti.addCompound("display")
    disp.setString("Name", name)

    val list = disp.getStringList("Lore")
    list.addAll(lore)

    val skull = nbti.addCompound("SkullOwner")
    skull.setString("Name", ownerName)
    skull.setString("Id", uuid)

    val texture = skull.addCompound("Properties").getCompoundList("textures").addCompound()
    texture.setString("Value", textureData)

    return nbti.item
}