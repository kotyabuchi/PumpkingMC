package com.github.kotyabuchi.pumpkingmc.CustomItem

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Recipe

interface Craftable {

    fun addRecipe(recipe: Recipe) {
        instance.server.addRecipe(recipe)
    }

    fun removeRecipe(recipeKey: NamespacedKey) {
        instance.server.removeRecipe(recipeKey)
    }
}