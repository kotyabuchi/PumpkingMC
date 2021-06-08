package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Enum.ItemType
import com.github.kotyabuchi.pumpkingmc.Enum.ToolPartType
import com.github.kotyabuchi.pumpkingmc.Menu.SmithingMenu
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.upperCamelCase
import com.github.kotyabuchi.pumpkingmc.Utility.getMiningLevel
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.floor

class MenuButtonSmithing(private val menu: SmithingMenu, private val toolPartType: ToolPartType): MenuButtonBase() {

    private val materialSlot = menu.getSlot(3, 1)
    private val resultItemSlot = menu.getSlot(3, 7)

    init {
        clickSound = null
        val lore = mutableListOf<String>()
        lore.add("&6Left Click: Craft 1 item")
        lore.add("&6Right Click: Craft 5 item")
        lore.add("&6Shift Left Click: Craft all item")
        menuItem = ItemStackGenerator(Material.ANVIL).setDisplayName("Craft").setMenuItemTag().setLore(lore).generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val materialSlotItem = menu.getItem(materialSlot) ?: return
        val resultSlotItem = menu.getItem(resultItemSlot)
        val materialAmount = toolPartType.materialAmount

        val craftAmount = when {
            event.isRightClick -> {
                5
            }
            event.isShiftClick -> {
                floor(materialSlotItem.amount / materialAmount.toDouble()).toInt()
            }
            else -> {
                1
            }
        }
        val needMaterialAmount = craftAmount * materialAmount

        if (materialSlotItem.amount >= needMaterialAmount) {
            if (menu.canCraft) {
                val resultItem = if (resultSlotItem != null && resultSlotItem.type == materialSlotItem.type) {
                    val item = resultSlotItem.clone()
                    item.amount += craftAmount
                    item
                } else {
                    val rarity = ItemExpansion(materialSlotItem).getRarity()
                    val item = ItemStackGenerator(materialSlotItem.clone()).setDisplayName((materialSlotItem.type.name + "_" + toolPartType.name).upperCamelCase()).addFakeEnchantment().generate()
                    item.amount = craftAmount
                    ItemExpansion(item, null, mutableListOf(), rarity, listOf(ItemType.TOOL_PART), toolPartType, item.type.getMiningLevel()).removeItemType(ItemType.MATERIAL).item
                }
                materialSlotItem.amount -= needMaterialAmount
                menu.setItem(materialSlot, materialSlotItem)
                menu.setItem(resultItemSlot, resultItem)
                player.getStatus().setOpeningMenu(menu)
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, .5f, 1.0f)
            }
        } else {
            player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, .5f, .5f)
        }
    }
}