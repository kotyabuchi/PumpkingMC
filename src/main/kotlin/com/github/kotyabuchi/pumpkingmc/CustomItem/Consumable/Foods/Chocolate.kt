package com.github.kotyabuchi.pumpkingmc.CustomItem.Consumable.Foods

import com.github.kotyabuchi.pumpkingmc.CustomItem.Food
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.addItemOrDrop
import com.github.kotyabuchi.pumpkingmc.Utility.normal
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit.addRecipe
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ShapedRecipe

object Chocolate: Food() {

    override val itemName: TextComponent = Component.text("Chocolate").normal()
    override val modelData: Int = 101
    override val material: Material = Material.APPLE
    override val foodLevel: Int = 2
    override val exhaustion: Float = 0f

    init {
        val recipe = ShapedRecipe(NamespacedKey(instance, itemType), itemStack)
            .shape("A", "B")
            .setIngredient('A', Material.COCOA_BEANS)
            .setIngredient('B', Material.GLASS_BOTTLE)
        addRecipe(recipe)
    }

    override fun consume(player: Player, event: PlayerItemConsumeEvent?) {
        super.consume(player, event)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.addItemOrDrop(player, ItemExpansion(Material.GLASS_BOTTLE).item)
    }
}