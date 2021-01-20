package com.github.kotyabuchi.pumpkingmc.Command

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kotyabuchi.pumpkingmc.Menu.HomeMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.addHome
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.readFile
import com.github.kotyabuchi.pumpkingmc.instance
import io.reactivex.rxjava3.core.Observable
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File
import java.util.concurrent.TimeUnit

class HomeCommand: CommandExecutor, TabCompleter, Listener {

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        if (sender is Player) {
            if (args.size == 1) {
                return mutableListOf("set", "remove")
            } else if (args.size >= 2) {
                return mutableListOf()
            }
        }
        return instance.onTabComplete(sender, cmd, label, args) ?: mutableListOf()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        val status = sender.getStatus()
        if (args.isEmpty()) {
            status.openMenu(HomeMenu(sender))
            return true
        } else if (args.size == 1 && args[0].toUpperCase() == "REMOVE") {
            status.openMenu(HomeMenu(sender, true))
            return true
        } else if (args.size == 2 && args[0].toUpperCase() == "SET") {
            val homeId = addHome(sender, args[1], sender.location)

            if (homeId == null) {
                sender.sendMessage("&eHomePoint [${args[1]}] の登録に失敗しました".colorS())
            } else {
                sender.sendMessage("&2HomePoint [${args[1]}] を登録しました".colorS())
            }
            return true
        }
        sender.sendMessage("&ahome: &r登録されているHomePointを表示します\n&ahome set <Name>: &r新しくHomePointを登録します\n&ahome remove: &rHomePointの削除メニューを表示する".colorS())
        return true
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val homeFolder = File(instance.dataFolder, "homes")
        if (!homeFolder.exists()) return
        if (homeFolder.listFiles() == null || homeFolder.listFiles().isEmpty()) {
            homeFolder.delete()
            return
        }
        val file = File(instance.dataFolder, "homes" + File.separator + player.uniqueId.toString() + ".json")
        if (!file.exists()) return

        val jsonArray = Json.parse(readFile(file)).asArray()
        if (jsonArray.isEmpty) {
            file.delete()
        } else {
            jsonArray.forEach {
                it as JsonObject
                instance.server.getWorld(it.getString("World", "world"))?.let {  world ->
                    addHome(player, it.getString("Name", ""), Location(world, it.getDouble("X", .0), it.getDouble("Y", .0), it.getDouble("Z", .0), it.getFloat("Yaw", 0f), 0f))
                }
            }
            Observable.interval(10, TimeUnit.MILLISECONDS).take(1).subscribe {
                file.delete()
            }
        }
    }
}