package com.github.kotyabuchi.pumpkingmc.Menu

import com.github.kotyabuchi.pumpkingmc.Menu.MenuItem.*
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.min

open class MenuBase(val title: String, _row: Int) {

    private val row = min(4, _row)
    private val menuSize = (row + 2) * 9
    private var slotAmount = menuSize
    private var hasFrame = false
    private val pages = mutableListOf(getInvTemp())
    private var prevMenu: MenuBase? = null
    private val buttonItems = mutableListOf(mutableMapOf<Int, MenuButtonBase>())

    private fun getInvTemp(): Inventory {
        return Bukkit.createInventory(null, menuSize, title.colorS())
    }

    fun setPrevMenu(menu: MenuBase): MenuBase {
        prevMenu = menu
        for (i in 0 until pages.size) {
            setMenuButton(BackPrevButton(menu), i, menuSize - 8)
        }
        hasFrame = true
        return this
    }

    fun setFrame(page: Int = 0): MenuBase {
        for (i in 0 until menuSize) {
            if (i < 9 || i > menuSize - 9 || i % 9 == 8 || i % 9 == 0) {
                setMenuButton(BlankButton(), page, i)
            }
        }
        slotAmount = menuSize - 9 - 9 - (2 * row)
        hasFrame = true
        return this
    }

    fun setMenuButton(menuButton: MenuButtonBase, page: Int = 0, slots: IntRange, checkNextPage: Boolean = true): MenuBase {
        slots.forEach {
            setMenuButton(menuButton, page, it, checkNextPage)
        }
        return this
    }

    fun setMenuButton(menuButton: MenuButtonBase, page: Int = 0, slot: Int? = getLastBlankSlot(page), checkNextPage: Boolean = true): MenuBase {
        if (slot == null) return this
        createPageIfNeed(page)
        buttonItems[page][slot] = menuButton
        pages[page].setItem(slot, menuButton.menuItem)
        if (checkNextPage && page > 0) {
            setMenuButton(BackPageButton(page, pages.size), page, menuSize - 7, false)
            setMenuButton(NextPageButton(page, pages.size), page - 1, menuSize - 3)
        }
        return this
    }

    fun removeMenuButton(slot: Int, page: Int = 0): MenuBase {
        buttonItems[page].remove(slot)
        pages[page].setItem(slot, null)
        return this
    }

    fun setItem(slot: Int, item: ItemStack?, page: Int = 0): MenuBase {
        removeMenuButton(slot, page)
        pages[page].setItem(slot, item)
        return this
    }

    fun removeItem(slot: Int, page: Int = 0): MenuBase {
        removeMenuButton(slot, page)
        pages[page].setItem(slot, null)
        return this
    }

    fun getItem(slot: Int, page: Int = 0): ItemStack? {
        return pages[page].getItem(slot)
    }

    fun getInventory(page: Int = 0): Inventory {
        createPageIfNeed(page)
        return pages[page]
    }

    fun getLastBlankSlot(page: Int = 0, passSlot: List<Int> = listOf()): Int? {
        createPageIfNeed(page)
        for ((index, menuItem) in pages[page].withIndex()) {
            if ((menuItem == null || menuItem.type.isAir) && !passSlot.contains(index)) return index
        }
        return null
    }

    fun getSlot(row: Int, column: Int): Int {
        return (row * 9) + column
    }

    fun getSlotAmount(): Int {
        return slotAmount
    }

    fun getButton(slot: Int, page: Int = 0): MenuButtonBase? {
        return buttonItems[page][slot]
    }

    fun hasButton(slot: Int, page: Int = 0): Boolean {
        createPageIfNeed(page)
        return buttonItems[page].containsKey(slot)
    }

    open fun doButtonClickEvent(slot: Int, event: InventoryClickEvent, page: Int = 0) {
        createPageIfNeed(page)
        val button = buttonItems[page][slot] ?: return
        button.clickEvent(event)
        val player = event.whoClicked as? Player ?: return
        playClickedButtonSound(button, player)
    }

    open fun doItemClickEvent(slot: Int, event: InventoryClickEvent, page: Int = 0) {}

    open fun doCloseMenuAction(player: Player) {}

    fun playClickedButtonSound(button: MenuButtonBase, player: Player) {
        val clickSound = button.clickSound
        if (clickSound != null) player.playSound(player.eyeLocation, clickSound, .5f, 1f)
    }

    fun createPageIfNeed(page: Int) {
        while (pages.size <= page) {
            pages.add(getInvTemp())
            if (hasFrame) setFrame(pages.size - 1)
        }
        while (buttonItems.size <= page) {
            buttonItems.add(mutableMapOf())
        }
    }
}