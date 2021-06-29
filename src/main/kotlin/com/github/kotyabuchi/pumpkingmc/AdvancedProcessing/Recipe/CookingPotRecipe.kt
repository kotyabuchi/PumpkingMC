package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Recipe

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.ProcessingType
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class CookingPotRecipe(override val recipeKey: NamespacedKey, private val result: ItemStack, vararg val ingredients: ItemStack): ProcessingRecipe {

    override val processingType: ProcessingType = ProcessingType.COOKING_POT
    override fun getResult(): ItemStack {
        return result
    }
}