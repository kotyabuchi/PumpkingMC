package com.github.kotyabuchi.pumpkingmc.Entity.Friendly

import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import org.bukkit.entity.AbstractVillager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EnchantedVillager: Listener {
    
    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {
        val villager = event.entity as? AbstractVillager ?: return
        villager.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 3, 1, false, false))
    }

    @EventHandler
    fun onClick(event: PlayerInteractEntityEvent) {
        val villager = event.rightClicked as? AbstractVillager ?: return
        val newRecipes = mutableListOf<MerchantRecipe>()
        villager.recipes.forEach { recipe ->
            val newIngredients = mutableListOf<ItemStack>()
            recipe.ingredients.forEach { item ->
                if (item != null && !item.type.isAir) newIngredients.add(ItemExpansion(item).item)
            }
            recipe.ingredients = newIngredients
            val newRecipe = MerchantRecipe(ItemExpansion(recipe.result).item, recipe.uses, recipe.maxUses, recipe.hasExperienceReward(), recipe.villagerExperience, recipe.priceMultiplier)
            newRecipe.ingredients = newIngredients
            newRecipes.add(newRecipe)
        }
        villager.recipes = newRecipes
    }
}
