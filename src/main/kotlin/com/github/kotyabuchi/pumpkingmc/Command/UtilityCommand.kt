package com.github.kotyabuchi.pumpkingmc.Command

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantment
import com.github.kotyabuchi.pumpkingmc.Menu.SoundMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.instance
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object UtilityCommand: CommandExecutor, TabCompleter {
    
    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        return instance.onTabComplete(sender, cmd, label, args) ?: mutableListOf()
    }
    
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        if (!sender.isOp) return true
        when (cmd.name) {
            "soundlist" -> {
                sender.getStatus().openMenu(SoundMenu())
            }
            "showsolidblock" -> {
                Material.values().forEach {
                    if (it.isBlock) {
                        sender.sendMessage(it.name + ": " + it.isSolid)
                    }
                }
            }
            "debugstuff" -> {
                val stuff = ItemStack(Material.STICK)
                val meta = stuff.itemMeta ?: return true
                meta.setDisplayName(ChatColor.GOLD.toString() + "DebugStuff")
                stuff.itemMeta = meta
                sender.inventory.addItem(stuff)
            }
            "allentity" -> {
                val entities = mutableMapOf<EntityType, Int>()
                instance.server.worlds.forEach { world->
                    world.entities.forEach {  entity ->
                        entities[entity.type] = (entities[entity.type] ?: 0) + 1
                    }
                }
                entities.forEach { t, u ->
                    sender.sendMessage("${t.name} : $u")
                }
            }
            "shownbti" -> {
                val item = sender.inventory.itemInMainHand
                if (!item.type.isAir) {
                    sender.sendMessage(NBTItem(item).toString())
                }
            }
            "showencha" -> {
                val item = sender.inventory.itemInMainHand
                if (!item.type.isAir) {
                    item.itemMeta?.let { meta ->
                        sender.sendMessage("====================")
                        meta.enchants.forEach { (enchant, level) ->
                            sender.sendMessage("${enchant.key.key} : $level")
                        }
                    }
                }
            }
            "customencha" -> {
                val item = sender.inventory.itemInMainHand
                if (!item.type.isAir) {
                    item.addUnsafeEnchantment(CustomEnchantment.TELEKINESIS, 1)
                }
            }
        }
        return true
    }
}
