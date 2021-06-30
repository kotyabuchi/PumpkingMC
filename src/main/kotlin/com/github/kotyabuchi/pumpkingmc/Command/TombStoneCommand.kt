package com.github.kotyabuchi.pumpkingmc.Command

import com.eclipsesource.json.JsonObject
import com.github.kotyabuchi.pumpkingmc.System.TombStone
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

object TombStoneCommand: CommandExecutor, TabCompleter, Listener {

    private val args1List = listOf("list", "view", "restore", "remove")

    private val restoreModePlayer = mutableMapOf<Player, Pair<String, String>>()

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        val result = mutableListOf<String>()
        if (!sender.isOp) return result
        when (args.size) {
            1 -> {
                args1List.forEach {
                    if (it.contains(args[0].toLowerCase())) result.add(it)
                }
            }
            2 -> {
                TombStone.tombStones.names().forEach {
                    result.add(it)
                }
            }
            3 -> {
                getPlayerTombStones(args[1])?.let { tombStones ->
                    repeat(tombStones.names().size) {
                        result.add(it.toString())
                    }
                }
            }
            4 -> {
                when (args[0].toLowerCase()) {
                    "restore" -> {
                        result.add("chest")
                        instance.server.onlinePlayers.forEach {
                            val name = it.name
                            if (name.toLowerCase().contains(args[3].toLowerCase())) result.add(name)
                        }
                    }
                }
            }
        }
        return result
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        if (!sender.isOp) return true
        if (args.isEmpty()) return true
        val tombStones = TombStone.tombStones
        when (args[0].toLowerCase()) {
            "list" -> {
                if (args.size < 2) return true
                val playersTombStones = tombStones[args[1]]?.asObject()
                if (playersTombStones == null) {
                    sender.sendMessage(Component.text("Player[${args[1]}]の墓石が見つかりません", NamedTextColor.RED))
                    return true
                }
                sendTombStoneList(sender, playersTombStones.names())
            }
            "view" -> {
                if (args.size < 3) return true
                val tombStone = getTombStoneWithMessage(sender, args) ?: return true
                val inventory = instance.server.createInventory(null, 9 * 5)
                TombStone.restoreItem(inventory, tombStone)
                sender.openInventory(inventory)
            }
            "restore" -> {
                if (args.size < 3) return true

                val tombStone = getTombStoneWithMessage(sender, args) ?: return true
                if (args.size == 4 && args[3].toUpperCase() == "CHEST") {
                    val playersTombStones = getPlayerTombStones(args[1]) ?: return true
                    restoreModePlayer[sender] = Pair(args[1], playersTombStones.names()[args[2].toInt()])
                    sender.sendMessage(Component.text("アイテムを入れるチェストをクリックしてください", NamedTextColor.GREEN))
                    return true
                }
                val target = if (args.size == 3) sender else instance.server.getPlayer(args[3])
                if (target == null) {
                    sender.sendMessage(Component.text("Player[${args[3]}]は存在しません", NamedTextColor.RED))
                    return true
                }
                TombStone.restoreItem(target, tombStone)
            }
            "remove" -> {
                if (args.size < 3) return true

                val tombStoneJson = getTombStoneWithMessage(sender, args) ?: return true
                val playersTombStones = getPlayerTombStones(args[1]) ?: return true
                val num = args[2].toIntOrNull() ?: return true
                val tombStoneUUID = playersTombStones.names()[num]
                val locationJson = tombStoneJson.get("Location").asObject()
                val location = Location(
                    instance.server.getWorld(UUID.fromString(locationJson.getString("world", ""))),
                    locationJson.getDouble("x", .0), locationJson.getDouble("y", .0), locationJson.getDouble("z", .0))
                val nearTombStones = location.getNearbyEntitiesByType(ArmorStand::class.java, .1)

                var tombStone: ArmorStand? = null
                nearTombStones.forEach {
                    if (it.uniqueId.toString() == tombStoneUUID) {
                        tombStone = it
                    }
                }
                if (tombStone == null) {
                    sender.sendMessage(Component.text("墓石が見つかりません", NamedTextColor.RED))
                    return true
                }
                tombStone?.let {
                    TombStone.removeTombStone(it)
                    sender.sendMessage(Component.text("墓石を削除しました", NamedTextColor.GREEN))
                }
            }
        }
        return true
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        if (!restoreModePlayer.contains(player)) return
        event.isCancelled = true
        val block = event.clickedBlock

        if (block == null) {
            player.sendMessage(Component.text("復元をキャンセルしました", NamedTextColor.RED))
            return
        }
        val chest = block.state as? Chest
        if (chest == null || (block.blockData as org.bukkit.block.data.type.Chest).type == org.bukkit.block.data.type.Chest.Type.SINGLE) {
            player.sendMessage(Component.text("復元先に指定できるのはダブルチェストだけです", NamedTextColor.RED))
            return
        }
        restoreModePlayer[player]?.let {
            val playersTombStones = getPlayerTombStones(it.first)
            if (playersTombStones == null) {
                player.sendMessage(Component.text("墓石が見つかりません", NamedTextColor.RED))
                return
            }
            val tombStone = getTombStone(playersTombStones, it.second)
            if (tombStone == null) {
                player.sendMessage(Component.text("墓石が見つかりません", NamedTextColor.RED))
                return
            }
            restoreModePlayer.remove(player)
            val inventory = chest.blockInventory
            TombStone.restoreItem(inventory, tombStone)
            player.sendMessage(Component.text("アイテムを復元しました", NamedTextColor.GREEN))
        }
    }

    private fun getTombStoneWithMessage(player: Player, args: Array<out String>): JsonObject? {
        val playerTombStone = getPlayerTombStones(args[1])
        if (playerTombStone == null) {
            player.sendMessage(Component.text("Player[${args[1]}]の墓石が見つかりません", NamedTextColor.RED))
            return null
        }

        val num = args[2].toIntOrNull()
        if (num == null) {
            player.sendMessage(Component.text("[${args[2]}]は数字ではありません", NamedTextColor.RED))
            sendTombStoneList(player, playerTombStone.names())
            return null
        }

        val tombStone = getTombStone(playerTombStone, num)
        if (tombStone == null) {
            player.sendMessage(Component.text("墓石が見つかりません", NamedTextColor.RED))
            sendTombStoneList(player, playerTombStone.names())
            return null
        }
        return tombStone
    }

    private fun getPlayerTombStones(target: String): JsonObject? {
        val tombStones = TombStone.tombStones
        return tombStones[target]?.asObject()
    }

    private fun getTombStone(playersTombStones: JsonObject, uuid: String): JsonObject? {
        return playersTombStones[uuid]?.asObject()
    }

    private fun getTombStone(playersTombStones: JsonObject, num: Int): JsonObject? {
        if (playersTombStones.names().size <= num) return null

        val tombStoneUUID = playersTombStones.names()[num]
        return getTombStone(playersTombStones, tombStoneUUID)?.asObject()
    }

    private fun sendTombStoneList(player: Player, list: List<String>) {
        player.sendMessage(Component.text("===================="))
        list.forEachIndexed { index, name ->
            player.sendMessage(Component.text("$index : $name"))
        }
        player.sendMessage(Component.text("===================="))
    }
}