package com.github.kotyabuchi.pumpkingmc.Command

import com.github.kotyabuchi.pumpkingmc.Menu.HomeMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ChatUtil
import com.github.kotyabuchi.pumpkingmc.Utility.addHome
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.Utility.normal
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.event.Listener

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
                sender.sendMessage(Component.text("HomePoint [${args[1]}] の登録に失敗しました", NamedTextColor.RED).normal())
            } else {
                sender.sendMessage(Component.text("HomePoint [${args[1]}] を登録しました", NamedTextColor.GREEN).normal())
            }
            return true
        }
        sender.sendMessage(ChatUtil.separator("Home"))
        sender.sendMessage(Component.text("/home: ", NamedTextColor.WHITE).normal()
            .append(Component.text("登録されているHomePoint一覧を表示します", NamedTextColor.GRAY)))
        sender.sendMessage(Component.text("/home set <Name>: ", NamedTextColor.WHITE).normal()
            .append(Component.text("<Name>の名前で新しくHomePointを登録します", NamedTextColor.GRAY)))
        sender.sendMessage(Component.text("/home remove: ", NamedTextColor.WHITE).normal()
            .append(Component.text("HomePointの削除用メニューを表示します", NamedTextColor.GRAY)))
        sender.sendMessage(ChatUtil.separator("Home", footer = true))
        return true
    }
}