package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.Enum.WoodType
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack

val aroundBlockFace = listOf(BlockFace.SELF, BlockFace.EAST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.WEST)

fun Material.isWood(): Boolean {
    return this == Material.ACACIA_LOG ||
            this == Material.BIRCH_LOG ||
            this == Material.DARK_OAK_LOG ||
            this == Material.JUNGLE_LOG ||
            this == Material.OAK_LOG ||
            this == Material.SPRUCE_LOG ||
            this == Material.STRIPPED_ACACIA_LOG ||
            this == Material.STRIPPED_BIRCH_LOG ||
            this == Material.STRIPPED_DARK_OAK_LOG ||
            this == Material.STRIPPED_JUNGLE_LOG ||
            this == Material.STRIPPED_OAK_LOG ||
            this == Material.STRIPPED_SPRUCE_LOG ||
            this == Material.ACACIA_WOOD ||
            this == Material.BIRCH_WOOD ||
            this == Material.JUNGLE_WOOD ||
            this == Material.OAK_WOOD ||
            this == Material.SPRUCE_WOOD ||
            this == Material.CRIMSON_STEM ||
            this == Material.WARPED_STEM ||
            this == Material.STRIPPED_CRIMSON_STEM ||
            this == Material.STRIPPED_WARPED_STEM ||
            this == Material.CRIMSON_HYPHAE ||
            this == Material.WARPED_HYPHAE ||
            this == Material.STRIPPED_CRIMSON_HYPHAE ||
            this == Material.STRIPPED_WARPED_HYPHAE
}
fun Material.isLeave(): Boolean {
    return this == Material.ACACIA_LEAVES ||
            this == Material.BIRCH_LEAVES ||
            this == Material.DARK_OAK_LEAVES ||
            this == Material.JUNGLE_LEAVES ||
            this == Material.OAK_LEAVES ||
            this == Material.SPRUCE_LEAVES
}
fun Material.isOre(): Boolean {
    return this == Material.COAL_ORE ||
            this == Material.IRON_ORE ||
            this == Material.GOLD_ORE ||
            this == Material.REDSTONE_ORE ||
            this == Material.LAPIS_ORE ||
            this == Material.DIAMOND_ORE ||
            this == Material.EMERALD_ORE ||
            this == Material.NETHER_GOLD_ORE ||
            this == Material.GLOWSTONE ||
            this == Material.NETHER_QUARTZ_ORE ||
            this == Material.GILDED_BLACKSTONE ||
            this == Material.ANCIENT_DEBRIS
}

fun Material.isLiquid(): Boolean = this == Material.WATER || this == Material.LAVA
fun Material.isGrass(): Boolean = this == Material.GRASS || this == Material.TALL_GRASS || this == Material.SEAGRASS || this == Material.TALL_SEAGRASS
fun Material.getWoodType(): WoodType? {
    return when {
        name.contains("ACACIA") -> WoodType.ACACIA
        name.contains("BIRCH") -> WoodType.BIRCH
        name.contains("DARK_OAK") -> WoodType.DARK_OAK
        name.contains("JUNGLE") -> WoodType.JUNGLE
        name.contains("OAK") -> WoodType.OAK
        name.contains("SPRUCE") -> WoodType.SPRUCE
        name.contains("CRIMSON") -> WoodType.CRIMSON
        name.contains("WARPED") -> WoodType.WARPED
        else -> null
    }
}

fun BlockFace.reverse(): BlockFace {
    return when (this) {
        BlockFace.UP -> BlockFace.DOWN
        BlockFace.DOWN -> BlockFace.UP
        BlockFace.EAST -> BlockFace.WEST
        BlockFace.NORTH -> BlockFace.SOUTH
        BlockFace.SOUTH -> BlockFace.NORTH
        BlockFace.WEST -> BlockFace.EAST
        BlockFace.SELF -> BlockFace.SELF
        BlockFace.NORTH_EAST -> BlockFace.SOUTH_WEST
        BlockFace.NORTH_WEST -> BlockFace.SOUTH_EAST
        BlockFace.SOUTH_EAST -> BlockFace.NORTH_WEST
        BlockFace.SOUTH_WEST -> BlockFace.NORTH_EAST
        BlockFace.EAST_NORTH_EAST -> BlockFace.WEST_SOUTH_WEST
        BlockFace.EAST_SOUTH_EAST -> BlockFace.WEST_NORTH_WEST
        BlockFace.NORTH_NORTH_EAST -> BlockFace.SOUTH_SOUTH_WEST
        BlockFace.NORTH_NORTH_WEST -> BlockFace.SOUTH_SOUTH_EAST
        BlockFace.SOUTH_SOUTH_EAST -> BlockFace.NORTH_NORTH_WEST
        BlockFace.SOUTH_SOUTH_WEST -> BlockFace.NORTH_NORTH_EAST
        BlockFace.WEST_NORTH_WEST -> BlockFace.EAST_SOUTH_EAST
        BlockFace.WEST_SOUTH_WEST -> BlockFace.EAST_NORTH_EAST
    }
}

fun Block.miningWithEvent(player: Player, itemStack: ItemStack, mainBlock: Block = this, isMultiBreak: Boolean = false) {
    val mineEvent = BlockMineEvent(this, player, isMultiBreak)
    instance.server.pluginManager.callEvent(mineEvent)
    if (!mineEvent.isCancelled) {
        val dropItems = mutableListOf<Item>()
        this.getDrops(itemStack, player).forEach { item ->
            val dropItem = mainBlock.world.dropItem(mainBlock.location.add(0.5, 0.0, 0.5), item)
            dropItems.add(dropItem)
        }
        val state = this.state
        if (this != mainBlock) {
            this.world.playSound(this.location.add(.5, .5, .5), this.soundGroup.breakSound, 1f, .75f)
            this.world.spawnParticle(Particle.BLOCK_CRACK, this.location.add(0.5, 0.5, 0.5), 20, .3, .3, .3, .0, this.blockData)
        }
        this.type = Material.AIR
        val dropEvent = BlockDropItemEvent(this, state, player, dropItems)
        instance.server.pluginManager.callEvent(dropEvent)
        if (dropEvent.items.isEmpty()) {
            dropItems.forEach { item ->
                item.remove()
            }
        }
    }
}

fun Block.destroyWithEffect(playSound: Boolean = true) {
    val effectMaterial = this.type
    if (playSound) this.world.playSound(this.location, Sound.BLOCK_STONE_BREAK, .8f, .75f)
    this.world.spawnParticle(Particle.BLOCK_CRACK, this.location.add(.5, .5, .5), 20, .3, .3, .3, 2.0, effectMaterial.createBlockData())
    this.type = Material.AIR
}