package com.github.kotyabuchi.pumpkingmc.CustomItem

import com.github.kotyabuchi.pumpkingmc.CustomEvent.PlayerGetItemEvent
import com.github.kotyabuchi.pumpkingmc.Enum.ItemType
import com.github.kotyabuchi.pumpkingmc.Enum.Rarity
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.instance
import com.google.gson.JsonObject
import de.tr7zw.nbtapi.NBTItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

class TransportAmulet: CustomItemMaster(Material.COMPASS, "TRANSPORT_AMULET") {

    private val itemName = Component.text("Transport Amulet", NamedTextColor.LIGHT_PURPLE)
    private val targetChestLocationKey = "TARGET_CHEST"
    private val activeStatusKey = "ACTIVE"

    init {
        val lore = mutableListOf<Component>()
        lore.add(Component.text("アイテムを拾った際、事前に登録したチェストに転送する。", NamedTextColor.GRAY))
        lore.add(Component.empty())
        lore.add(Component.text("転送先チェスト: ", NamedTextColor.WHITE)
            .append(Component.text("未登録", NamedTextColor.RED)))

        val item = ItemExpansion(baseMaterial, itemName, lore, Rarity.RARE, listOf(ItemType.AMULET)).item
        val nbti = NBTItem(item)
        nbti.setBoolean(itemKey, true)
        val recipe = ShapedRecipe(NamespacedKey(instance, itemKey), nbti.item)
        recipe.shape(" A ", "BCB", " D ")
        recipe.setIngredient('A', Material.STRING)
        recipe.setIngredient('B', Material.ENDER_PEARL)
        recipe.setIngredient('C', Material.EMERALD)
        recipe.setIngredient('D', Material.ENDER_EYE)

        registerRecipe(recipe)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        val hand = event.hand ?: return
        if (hand != EquipmentSlot.HAND) return
        val item = player.inventory.getItem(hand) ?: return

        if (!isCustomItem(item)) return
        if (!event.action.name.startsWith("RIGHT")) return
        event.isCancelled = true

        if (player.isSneaking) {
            val block = event.clickedBlock ?: return
            if (block.state !is Container) return
            val loc = block.location
            val meta = item.itemMeta ?: return
            val lore = meta.lore ?: return
            val locStr = "${loc.world.name} : ${loc.x}, ${loc.y}, ${loc.z}"
            lore[lore.size - 1] = "&f転送先チェスト: $locStr".colorS()
            meta.lore = lore
            item.itemMeta = meta
            val nbti = NBTItem(item)
            val json = JsonObject()
            json.addProperty("world", loc.world.name)
            json.addProperty("x", loc.x)
            json.addProperty("y", loc.y)
            json.addProperty("z", loc.z)
            nbti.setObject(targetChestLocationKey, json)
            player.inventory.setItem(hand, nbti.item)
        } else {
            val result: ItemStack
            val nbti = NBTItem(item)
            result = if (nbti.hasKey(activeStatusKey)) {
                player.sendActionBar('&', "&cDeactivated")
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                nbti.removeKey(activeStatusKey)
                ItemExpansion(nbti.item).removeDummyEnchant().item
            } else {
                player.sendActionBar('&', "&aActivated")
                player.playSound(player.location.add(0.0, 2.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
                nbti.setBoolean(activeStatusKey, true)
                ItemExpansion(nbti.item).setDummyEnchant().item
            }
            player.inventory.setItem(hand, result)
        }
    }

    @EventHandler
    fun onPickup(event: PlayerGetItemEvent) {
        val item = event.item ?: return

        val hasItem = hasItem(event.player)
        if (!hasItem.first) return
        val amulet = hasItem.second ?: return

        val nbti = NBTItem(amulet)
        if (!nbti.hasKey(activeStatusKey)) return
        val targetStorageLocJson = nbti.getObject(targetChestLocationKey, JsonObject::class.java) ?: return
        val targetStorageLoc = Location(instance.server.getWorld(targetStorageLocJson["world"].asString), targetStorageLocJson["x"].asDouble, targetStorageLocJson["y"].asDouble, targetStorageLocJson["z"].asDouble)
        val targetStorage = targetStorageLoc.block
        val storage = targetStorage.state as? Container ?: return
        val storageInv = storage.inventory
        val result = storageInv.addItem(item)
        if (result.isEmpty()) {
            event.item = null
        } else {
            event.item = item
        }
    }
}