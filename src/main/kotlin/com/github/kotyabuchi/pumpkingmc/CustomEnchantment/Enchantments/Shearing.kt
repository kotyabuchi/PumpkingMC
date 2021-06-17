package com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentMaster
import com.github.kotyabuchi.pumpkingmc.Enum.ToolType
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Chicken
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Sheep
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.round
import kotlin.random.Random

object Shearing: CustomEnchantmentMaster("SHEARING") {

    private const val maxLevel = 3
    private val itemTarget = EnchantmentTarget.WEAPON

    override fun getProbability(expCost: Int): Int {
        return round(expCost.toDouble() / rarity.weight).toInt()
    }

    override fun getMaxLevel(): Int {
        return maxLevel
    }

    override fun getItemTarget(): EnchantmentTarget {
        return itemTarget
    }

    override fun canEnchantItem(item: ItemStack): Boolean {
        return ToolType.SWORD.includes(item)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEntityEvent) {
        if (event.isCancelled) return
        val player = event.player
        val usedItem = player.inventory.getItem(event.hand)
        val meta = usedItem?.itemMeta ?: return
        if (!meta.hasEnchant(this)) return
        val entity = event.rightClicked as? LivingEntity ?: return

        if (entity.health <= 0 || entity.noDamageTicks > 0) return
        val dropItem = when (entity) {
            is Sheep -> {
                if (entity.isSheared) return
                entity.isSheared = true
                val color = entity.color ?: DyeColor.WHITE
                ItemStack(Material.valueOf("${color}_WOOL"), Random.nextInt(3) + 1)
            }
            is Chicken -> {
                ItemStack(Material.FEATHER, Random.nextInt(3) + 1)
            }
            else -> return
        }
        val damage = 4 - meta.getEnchantLevel(this)
        if (damage > 0) entity.damage(damage.toDouble(), player)
        val world = entity.world
        val droppedItem = world.dropItem(entity.eyeLocation, dropItem)
        val dropEvent = EntityDropItemEvent(entity, droppedItem)
        instance.callEvent(dropEvent)
        if (dropEvent.isCancelled) droppedItem.remove()
        entity.world.playSound(entity.eyeLocation, Sound.ENTITY_SHEEP_SHEAR, 1f, 1f)
        instance.callEvent(PlayerItemDamageEvent(player, usedItem, damage))
    }
}