package com.github.kotyabuchi.pumpkingmc.CustomItem

import com.github.kotyabuchi.pumpkingmc.CustomItem.Consumable.Foods.Chocolate
import com.github.kotyabuchi.pumpkingmc.CustomItem.Consumable.Foods.HoneyedApple

enum class CustomItem(val itemClass: ItemMaster) {
    CHOCOLATE(Chocolate),
    HONEYED_APPLE(HoneyedApple)
}