package com.github.kotyabuchi.pumpkingmc.Enum

import com.github.kotyabuchi.pumpkingmc.Utility.beginWithUpperCase
import org.bukkit.Material

enum class ToolPartType(val icon: Material, val materialAmount: Int, val isHead: Boolean, val tool: ToolType?) {
    PICKAXE_HEAD(Material.IRON_PICKAXE, 3, true, ToolType.PICKAXE),
    AXE_HEAD(Material.IRON_AXE, 3, true, ToolType.AXE),
    SHOVEL_HEAD(Material.IRON_SHOVEL, 1, true, ToolType.SHOVEL),
    HOE_HEAD(Material.IRON_HOE, 2, true, ToolType.HOE),
    SWORD_BLADE(Material.IRON_SWORD, 2, true, ToolType.SWORD),
    BATTLEAXE_BLADE(Material.IRON_AXE, 3, true, ToolType.BATTLEAXE),
    BOW_LIMB(Material.BOW, 3, true, ToolType.BOW),
    BOW_STRING(Material.STRING, 3, false, null),
    TOOL_BINDING(Material.FLINT, 1, false, null),
    TOOL_ROD(Material.STICK, 1, false, null);

    fun getRegularName(): String {
        return name.beginWithUpperCase()
    }
}