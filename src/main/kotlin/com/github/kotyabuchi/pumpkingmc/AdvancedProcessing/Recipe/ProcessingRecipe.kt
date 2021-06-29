package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Recipe

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.ProcessingType
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Recipe

interface ProcessingRecipe: Recipe {

    val recipeKey: NamespacedKey
    val processingType: ProcessingType
}