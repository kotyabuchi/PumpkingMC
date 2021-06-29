package com.github.kotyabuchi.pumpkingmc.AdvancedProcessing

import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility.AlchemyCauldron
import com.github.kotyabuchi.pumpkingmc.AdvancedProcessing.Facility.ProcessingFacility

enum class ProcessingType(facility: ProcessingFacility) {
    ALCHEMY_CAULDRON(AlchemyCauldron)
}