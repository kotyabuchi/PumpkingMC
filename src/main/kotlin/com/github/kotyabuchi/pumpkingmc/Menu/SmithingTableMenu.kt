package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Enum.ItemType
import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.BlankButton
import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.SmithingButton
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.addItemOrDrop
import com.github.kotyabuchi.pumpkingmc.Utility.canUseToolHead
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.scheduler.BukkitRunnable

class SmithingMenu(private val toolPartType: ToolPartType): MenuBase("Smithing - ${toolPartType.name.upperCamelCase()}", 6) {

    private val materialSlot = getSlot(3, 1)
    private val resultLineSlotRange = getSlot(3, 2) .. getSlot(3, 6)
    private val resultItemSlot = getSlot(3, 7)
    var canCraft = false

    init {
        setMenuButton(BlankButton(Material.IRON_BARS), 0, 0 .. 8)
        setMenuButton(BlankButton(Material.IRON_BARS), 0, getSlot(5, 0) .. getSlot(5, 8))
        setMenuButton(SmithingButton(this, toolPartType), 0, getSlot(2, 4))
        setMenuButton(BlankButton(Material.RED_STAINED_GLASS_PANE), 0, resultLineSlotRange)
        setMenuButton(BlankButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE), 0, resultItemSlot)
        while (getLastBlankSlot(0, listOf(materialSlot)) != null) {
            setMenuButton(BlankButton(), 0, getLastBlankSlot(0, listOf(materialSlot)))
        }
    }

    override fun doCloseMenuAction(player: Player) {
        getInventory(0).getItem(materialSlot)?.let { material ->
            player.inventory.addItemOrDrop(player, material)
        }
        if (!hasButton(0, resultItemSlot)) {
            getInventory(0).getItem(resultItemSlot)?.let { resultItem ->
                player.inventory.addItemOrDrop(player, resultItem)
            }
        }
    }

    override fun doItemClickEvent(slot: Int, event: InventoryClickEvent, page: Int) {
        object : BukkitRunnable() {
            override fun run() {
                val materialSlotItem = getItem(materialSlot)
                val resultSlotItem = getItem(resultItemSlot)

                if (resultSlotItem == null) setMenuButton(BlankButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE), 0, resultItemSlot)
                if (materialSlotItem != null &&
                        ItemExpansion(materialSlotItem).getItemTypes().contains(ItemType.MATERIAL) &&
                        !(toolPartType.isHead && !materialSlotItem.type.canUseToolHead()) &&
                        materialSlotItem.amount >= toolPartType.materialAmount &&
                        (hasButton(resultItemSlot) || materialSlotItem.type == resultSlotItem?.type)) {
                    setMenuButton(BlankButton(Material.LIME_STAINED_GLASS_PANE), 0, resultLineSlotRange)
                    canCraft = true
                } else {
                    setMenuButton(BlankButton(Material.RED_STAINED_GLASS_PANE), 0, resultLineSlotRange)
                    canCraft = false
                }
            }
        }.runTaskLater(instance, 1)
    }
}