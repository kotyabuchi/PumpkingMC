package com.github.kotyabuchi.pumpkingmc.System

import com.github.kotyabuchi.pumpkingmc.Enum.ItemType
import com.github.kotyabuchi.pumpkingmc.Enum.Rarity
import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent

class CombineToolStation: Listener {

    @EventHandler
    fun onCraft(event: PrepareAnvilEvent) {
        val inv = event.inventory

        val material1 = inv.getItem(0) ?: return
        val material2 = inv.getItem(1) ?: return
        val expansion1 = ItemExpansion(material1)
        val expansion2 = ItemExpansion(material2)

        Bukkit.broadcastMessage("================================")

        if (!expansion1.getItemTypes().contains(ItemType.TOOL_PART) || !expansion2.getItemTypes().contains(ItemType.TOOL_PART)) return
        val toolPartType1 = expansion1.toolPartType ?: return
        val toolPartType2 = expansion2.toolPartType ?: return

        if (!toolPartType1.isHead || toolPartType2.isHead) return
        if (toolPartType1 == ToolPartType.BOW_LIMB && toolPartType2 != ToolPartType.BOW_STRING) return
        if (toolPartType1 != ToolPartType.BOW_LIMB && toolPartType2 == ToolPartType.BOW_STRING) return

        val toolType = toolPartType1.tool ?: return
        val result = ItemExpansion(Material.valueOf(expansion1.materialMiningLevel?.name + "_" + toolType.name), null, mutableListOf(), Rarity.COMMON, listOf(ItemType.TOOL), 1, null, expansion1.materialMiningLevel).item

        event.result = result
    }
}