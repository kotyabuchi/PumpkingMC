package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.BlankButton
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.CombineToolButton
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.addItemOrDrop
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.scheduler.BukkitRunnable

class ToolStationMenu: MenuBase("Tool Station", 6) {

    private val materialSlot1 = getSlot(1, 1)
    private val materialSlot2 = getSlot(1, 3)
    private val materialSlot3 = getSlot(1, 5)
    private val resultItemSlot = getSlot(4, 7)
    private val materialLines = listOf(listOf(getSlot(2, 1), getSlot(3, 1), getSlot(4, 1), getSlot(4, 2)),
            listOf(getSlot(2, 3), getSlot(3, 3), getSlot(4, 3), getSlot(4, 4)),
            listOf(getSlot(2, 5), getSlot(3, 5), getSlot(4, 5), getSlot(4, 6)))

    private var canCrafts = mutableListOf(false, false, false)

    init {
        setFrame()
        materialLines.forEach { line->
            line.forEach {
                setMenuButton(BlankButton(Material.RED_STAINED_GLASS_PANE), 0, it)
            }
        }
        setMenuButton(BlankButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE), 0, resultItemSlot)
        setMenuButton(CombineToolButton(this), 0, getSlot(2, 7))

        val passSlot = mutableListOf(materialSlot1, materialSlot2, materialSlot3)

        while (getLastBlankSlot(0, passSlot) != null) {
            setMenuButton(BlankButton(), 0, getLastBlankSlot(0, passSlot))
        }
    }

    override fun doCloseMenuAction(player: Player) {
        val inv = getInventory(0)
        val playerInv = player.inventory
        inv.getItem(materialSlot1)?.let {
            playerInv.addItemOrDrop(player, it)
        }
        inv.getItem(materialSlot2)?.let {
            playerInv.addItemOrDrop(player, it)
        }
        inv.getItem(materialSlot3)?.let {
            playerInv.addItemOrDrop(player, it)
        }
        if (!hasButton(resultItemSlot)) {
            inv.getItem(resultItemSlot)?.let {
                playerInv.addItemOrDrop(player, it)
            }
        }
    }

    override fun doItemClickEvent(slot: Int, event: InventoryClickEvent, page: Int) {
        object : BukkitRunnable() {
            override fun run() {
                checkCanCraft(event)
            }
        }.runTaskLater(instance, 1)
    }

    private fun checkCanCraft(event: InventoryClickEvent) {
        Bukkit.broadcastMessage("================================")
        var canCraft = true

        val materials = listOf(getItem(materialSlot1), getItem(materialSlot2), getItem(materialSlot3))
        materials.forEachIndexed { index, itemStack ->
            var currentMaterial = true
            if (itemStack == null) {
                currentMaterial = false
            } else {
                val itemExpansion = ItemExpansion(itemStack)
                val toolPartType = itemExpansion.toolPartType

                if (toolPartType == null) {
                    currentMaterial = false
                } else {
                    if (index != 0 && toolPartType != ToolPartType.BOW_LIMB && toolPartType.isHead) {
                        if (materials[0] == null) {
                            setItem(materialSlot1, itemStack)
                            removeItem(materialSlot1 + (index * 2))
                            checkCanCraft(event)
                            return
                        } else {
                            currentMaterial = false
                        }
                    }
                    if (index == 2 && toolPartType == ToolPartType.BOW_LIMB) {
                        if (materials[0] == null) {
                            setItem(materialSlot1, itemStack)
                            removeItem(materialSlot1 + (index * 2))
                            checkCanCraft(event)
                            return
                        } else if (materials[1] == null) {
                            setItem(materialSlot2, itemStack)
                            removeItem(materialSlot1 + (index * 2))
                            checkCanCraft(event)
                            return
                        } else {
                            currentMaterial = false
                        }
                    }
                    if (index != 1 && toolPartType == ToolPartType.TOOL_BINDING) {
                        if (materials[1] == null) {
                            setItem(materialSlot2, itemStack)
                            removeItem(materialSlot1 + (index * 2))
                            checkCanCraft(event)
                            return
                        } else {
                            currentMaterial = false
                        }
                    }
                    if (index != 2 && toolPartType == ToolPartType.TOOL_ROD) {
                        if (materials[2] == null) {
                            setItem(materialSlot3, itemStack)
                            removeItem(materialSlot1 + (index * 2))
                            checkCanCraft(event)
                            return
                        } else {
                            currentMaterial = false
                        }
                    }
                    if (index != 2 && toolPartType == ToolPartType.BOW_STRING) {
                        if (materials[2] == null) {
                            setItem(materialSlot3, itemStack)
                            removeItem(materialSlot1 + (index * 2))
                            checkCanCraft(event)
                            return
                        } else {
                            currentMaterial = false
                        }
                    }
                    if (index == 0) {
                        if (toolPartType == ToolPartType.BOW_LIMB) {
                            if ((materials[1] != null && ItemExpansion(materials[1]!!).toolPartType != ToolPartType.BOW_LIMB) ||
                                    (materials[2] != null && ItemExpansion(materials[2]!!).toolPartType != ToolPartType.BOW_STRING)) {
                                currentMaterial = false
                            }
                        } else if (toolPartType.isHead) {
                            if ((materials[1] != null && ItemExpansion(materials[1]!!).toolPartType != ToolPartType.TOOL_BINDING) ||
                                    (materials[2] != null && ItemExpansion(materials[2]!!).toolPartType != ToolPartType.TOOL_ROD)) {
                                currentMaterial = false
                            }
                        }
                    } else if (index == 1) {
                        if (toolPartType == ToolPartType.BOW_LIMB) {
                            if ((materials[0] != null && ItemExpansion(materials[0]!!).toolPartType != ToolPartType.BOW_LIMB) ||
                                    (materials[2] != null && ItemExpansion(materials[2]!!).toolPartType != ToolPartType.BOW_STRING)) {
                                currentMaterial = false
                            }
                        } else if (toolPartType == ToolPartType.TOOL_BINDING) {
                            if ((materials[0] != null && ItemExpansion(materials[0]!!).toolPartType?.isHead != true) ||
                                    (materials[2] != null && ItemExpansion(materials[2]!!).toolPartType != ToolPartType.TOOL_ROD)) {
                                currentMaterial = false
                            }
                        }
                    } else if (index == 2) {
                        if (toolPartType == ToolPartType.BOW_STRING &&
                                (materials[0] != null && ItemExpansion(materials[0]!!).toolPartType != ToolPartType.BOW_LIMB) ||
                                (materials[1] != null && ItemExpansion(materials[1]!!).toolPartType != ToolPartType.BOW_LIMB)) {
                            currentMaterial = false
                        } else if (toolPartType == ToolPartType.TOOL_ROD) {
                            if ((materials[0] != null && ItemExpansion(materials[0]!!).toolPartType?.isHead != true) ||
                                    (materials[1] != null && ItemExpansion(materials[1]!!).toolPartType != ToolPartType.TOOL_BINDING)) {
                                currentMaterial = false
                            }
                        }
                    }
                }
            }
            if (!currentMaterial) canCraft = currentMaterial
            setCanCraft(currentMaterial, index)
        }
        if (!canCraft) return
        Bukkit.broadcastMessage("Can craft")

//        val expansions = listOf(ItemExpansion(materials[0]!!), ItemExpansion(materials[1]!!), ItemExpansion(materials[2]!!))

//                if (!expansion1.getItemTypes().contains(ItemType.TOOL_PART) ||
//                        !expansion2.getItemTypes().contains(ItemType.TOOL_PART) ||
//                        !expansion3.getItemTypes().contains(ItemType.TOOL_PART)) return
//                val toolPartType1 = expansion1.toolPartType ?: return
//                val toolPartType2 = expansion2.toolPartType ?: return
//                val toolPartType3 = expansion3.toolPartType ?: return
//                Bukkit.broadcastMessage("Pass 1")
//
//                if (!toolPartType1.isHead) return
//                Bukkit.broadcastMessage("Pass 2")
//                if (toolPartType1 == ToolPartType.BOW_LIMB || toolPartType2 == ToolPartType.BOW_LIMB) {
//                    if (toolPartType1 != toolPartType2 || toolPartType3 != ToolPartType.BOW_STRING) return
//                    Bukkit.broadcastMessage("Pass 3")
//                } else {
//                    if (toolPartType2.isHead || toolPartType3.isHead) return
//                    Bukkit.broadcastMessage("Pass 3")
//                    if (toolPartType1.tool == null) return
//                }
    }

    private fun setCanCraft(canCraft: Boolean, slot: Int) {
        canCrafts[slot] = canCraft
        if (canCraft) {
            materialLines[slot].forEach {
                setMenuButton(BlankButton(Material.LIME_STAINED_GLASS_PANE), 0, it)
            }
        } else {
            materialLines[slot].forEach {
                setMenuButton(BlankButton(Material.RED_STAINED_GLASS_PANE), 0, it)
            }
        }
    }
}