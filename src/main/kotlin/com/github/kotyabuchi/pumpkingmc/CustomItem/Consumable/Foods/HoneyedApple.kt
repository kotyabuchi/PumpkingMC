package com.github.kotyabuchi.pumpkingmc.CustomItem.Consumable.Foods

import com.github.kotyabuchi.pumpkingmc.CustomItem.Craftable
import com.github.kotyabuchi.pumpkingmc.CustomItem.Food
import com.github.kotyabuchi.pumpkingmc.Utility.normal
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object HoneyedApple: Food(), Craftable {

    override val itemName: TextComponent = Component.text("Honeyed Apple").normal()
    override val modelData: Int = 100
    override val material: Material = Material.APPLE
    override val foodLevel: Int = 14
    override val exhaustion: Float = 0f

    init {
        val result = itemStack.clone()
        result.amount = 4
        val recipe = ShapelessRecipe(NamespacedKey(instance, "Honeyed_Apple"), result)
        repeat(4) {
            recipe.addIngredient(Material.APPLE)
        }
        recipe.addIngredient(Material.HONEY_BOTTLE)
        addRecipe(recipe)
    }

    override fun consume(player: Player, event: PlayerItemConsumeEvent?) {
        super.consume(player, event)
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1))
    }
}