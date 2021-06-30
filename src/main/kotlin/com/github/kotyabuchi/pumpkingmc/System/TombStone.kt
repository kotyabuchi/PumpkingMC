package com.github.kotyabuchi.pumpkingmc.System

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kotyabuchi.pumpkingmc.Utility.*
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.math.roundToInt

object TombStone: Listener {

    private val tombStoneFile = File(instance.dataFolder, "TombStones.json")
    val tombStones: JsonObject
    val tombStoneKey = NamespacedKey(instance, "TombStone")

    init {
        tombStones = if (tombStoneFile.exists()) {
            try {
                Json.parse(Files.readString(tombStoneFile.toPath())).asObject()
            } catch (e: Exception) {
                e.printStackTrace()
                JsonObject()
            }
        } else {
            tombStoneFile.createNewFile()
            JsonObject()
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val inv = player.inventory
        val keepItems = event.itemsToKeep

        val json = JsonObject()
        val equipments = JsonObject()
        val storage = JsonObject()

        EquipmentSlot.values().forEach {
            if (it != EquipmentSlot.HAND) {
                val itemStack = inv.getItem(it)
                if (itemStack != null && !itemStack.type.isAir && !keepItems.contains(itemStack)) equipments.set(it.name, itemStack.toSerializedString())
            }
        }
        inv.storageContents.forEachIndexed { index, itemStack ->
            if (itemStack != null && !itemStack.type.isAir && !keepItems.contains(itemStack)) storage.set(index.toString(), itemStack.toSerializedString())
        }

        if (equipments.isEmpty && storage.isEmpty) return
        val tombStoneLoc = player.location.block.location.toCenterLocation()
        val locationJson = JsonObject()
        locationJson.set("world", tombStoneLoc.world.uid.toString())
        locationJson.set("x", tombStoneLoc.x)
        locationJson.set("y", tombStoneLoc.y)
        locationJson.set("z", tombStoneLoc.z)
        json.set("Location", locationJson)
        json.set("Equipment", equipments)
        json.set("Storage", storage)

        val playersTombStones = tombStones.get(player.uniqueId.toString())?.asObject() ?: JsonObject()

        val tombStoneItem = ItemStack(Material.OAK_SIGN)
        tombStoneItem.editMeta {
            it.setCustomModelData(100)
        }
        tombStoneLoc.y = player.location.y
        EntityType.ARMOR_STAND.entityClass?.let {
            player.world.spawn(tombStoneLoc, it) { stand ->
                stand as ArmorStand
                stand.equipment?.setHelmet(tombStoneItem, true)
                stand.isSilent = true
                stand.isVisible = false
                playersTombStones.set(stand.uniqueId.toString(), json)
                stand.persistentDataContainer.set(tombStoneKey, PersistentDataType.STRING, player.uniqueId.toString())
                stand.addPassenger(
                    player.world.spawn(tombStoneLoc, it) { nameStand ->
                        nameStand as ArmorStand
                        nameStand.customName(
                            Component.text("R.I.P [${player.name}]", NamedTextColor.WHITE, TextDecoration.BOLD).normal()
                        )
                        nameStand.isCustomNameVisible = true
                        nameStand.isMarker = true
                        nameStand.isSilent = true
                        nameStand.isVisible = false
                    }
                )
            }
        }
        event.drops.clear()
        tombStones.set(player.uniqueId.toString(), playersTombStones)

        val lore = listOf("X: ${tombStoneLoc.x.roundToInt()}", "Y: ${tombStoneLoc.y.roundToInt()}", "Z: ${tombStoneLoc.z.roundToInt()}")
        val deathLog = ItemStackGenerator(Material.PAPER).setDisplayName("R.I.P ${player.name}").setLore(lore).addFakeEnchantment().generate()
        event.itemsToKeep.add(deathLog)
    }

    @EventHandler
    fun onClick(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val tombStone = event.rightClicked as? ArmorStand ?: return
        val pdc = tombStone.persistentDataContainer
        val uuid = pdc.get(tombStoneKey, PersistentDataType.STRING) ?: return
        event.isCancelled = true
        if (uuid.isEmpty()) return
        if (player.uniqueId != UUID.fromString(uuid)) {
            player.showTitle(Title.title(Component.empty(), Component.text("他人の墓荒らしは良くないよ", NamedTextColor.RED, TextDecoration.BOLD).normal()))
            return
        }
        loadTombStoneItems(player, tombStone)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity as? ArmorStand ?: return
        if (entity.persistentDataContainer.has(tombStoneKey, PersistentDataType.STRING)) event.isCancelled = true
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val tombStone = event.entity as? ArmorStand ?: return
        if (!tombStone.persistentDataContainer.has(tombStoneKey, PersistentDataType.STRING)) return
        event.isCancelled = true
        event.damage = 0.0
        val player = event.damager as? Player ?: return
        val pdc = tombStone.persistentDataContainer
        val uuid = pdc.get(tombStoneKey, PersistentDataType.STRING) ?: return
        event.isCancelled = true
        if (uuid.isEmpty()) return
        if (player.uniqueId != UUID.fromString(uuid))  {
            player.showTitle(Title.title(Component.empty(), Component.text("他人の墓荒らしは良くないよ", NamedTextColor.RED, TextDecoration.BOLD).normal()))
            return
        }
        loadTombStoneItems(player, tombStone)
    }

    private fun loadTombStoneItems(player: Player, tombStone: ArmorStand) {
        val pdc = tombStone.persistentDataContainer
        val uuid = pdc.get(tombStoneKey, PersistentDataType.STRING) ?: return

        object : BukkitRunnable() {
            val loc = tombStone.location
            var count = 0
            override fun run() {
                val playersTombStones = tombStones.get(uuid)?.asObject()
                val tombStoneUUID = tombStone.uniqueId.toString()
                val tombItems = playersTombStones?.get(tombStoneUUID)?.asObject()

                if (tombItems == null || player.isDead) {
                    cancel()
                } else {
                    if (count >= 2) {
                        restoreItem(player, tombItems)
                        removeTombStone(tombStone)

                        cancel()
                    }
                    player.world.playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1.5f, .5f)
                }
                count++
            }
        }.runTaskTimer(instance, 0, 10)
    }

    fun restoreItem(inventory: Inventory, tombStone: JsonObject): List<ItemStack> {
        val equipmentItems = tombStone.get("Equipment").asObject()
        val storageItems = tombStone.get("Storage").asObject()

        val inventoryBackup = mutableListOf<ItemStack>()

        storageItems.forEach {
            val serializedStr = storageItems.getString(it.name, null)
            if (serializedStr != null) {
                val slot = it.name.toInt()
                val storageItem = inventory.getItem(slot)
                if (storageItem != null && !storageItem.type.isAir) inventoryBackup.add(storageItem)
                inventory.setItem(slot,ItemUtil.deserializeItem(serializedStr))
            }
        }

        if (inventory is PlayerInventory) {
            equipmentItems.forEach {
                val serializedStr = equipmentItems.getString(it.name, null)
                if (serializedStr != null) {
                    val equipmentItem = inventory.getItem(EquipmentSlot.valueOf(it.name))
                    if (equipmentItem != null && !equipmentItem.type.isAir) inventoryBackup.add(equipmentItem)
                    inventory.setItem(EquipmentSlot.valueOf(it.name), ItemUtil.deserializeItem(serializedStr))
                }
            }
        } else {
            equipmentItems.forEachIndexed { index, item ->
                val serializedStr = equipmentItems.getString(item.name, null)
                if (serializedStr != null) {
                    val slot = 9 * 4 + index
                    val equipmentItem = inventory.getItem(slot)
                    if (equipmentItem != null && !equipmentItem.type.isAir) inventoryBackup.add(equipmentItem)
                    inventory.setItem(slot, ItemUtil.deserializeItem(serializedStr))
                }
            }
        }

        return inventoryBackup
    }

    fun restoreItem(player: Player, tombStone: JsonObject) {
        val inventory = player.inventory
        restoreItem(inventory, tombStone).forEach { backupItem ->
            inventory.addItemOrDrop(player, backupItem)
        }
    }

    fun removeTombStone(tombStone: ArmorStand) {
        val pdc = tombStone.persistentDataContainer
        val uuid = pdc.get(tombStoneKey, PersistentDataType.STRING) ?: return
        val playersTombStones = tombStones.get(uuid)?.asObject() ?: return
        val tombStoneUUID = tombStone.uniqueId.toString()

        pdc.set(tombStoneKey, PersistentDataType.STRING, "")
        playersTombStones.remove(tombStoneUUID)
        tombStones.set(uuid, playersTombStones)

        tombStone.passengers.forEach {
            it.remove()
        }
        tombStone.remove()
    }

    fun saveTombStoneFile() {
        FileUtil.saveFile(tombStoneFile, tombStones)
        println("&a[System]TombStoneを保存しました".colorS())
    }
}