package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Recipe

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.ProcessingType
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class StoneMillRecipe(override val recipeKey: NamespacedKey, private val result: ItemStack, vararg val ingredients: ItemStack) : ProcessingRecipe {

    override val processingType: ProcessingType = ProcessingType.STONE_MILL
    override fun getResult(): ItemStack {
        return result
    }
}