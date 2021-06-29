package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility.AlchemyCauldron
import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility.CookingPot
import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility.ProcessingFacility
import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility.StoneMill

enum class ProcessingType(facility: ProcessingFacility) {
    ALCHEMY_CAULDRON(AlchemyCauldron),
    COOKING_POT(CookingPot),
    STONE_MILL(StoneMill)
}