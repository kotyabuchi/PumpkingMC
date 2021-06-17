package com.github.kotyabuchi.pumpkingmc.Command

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantment
import com.github.kotyabuchi.pumpkingmc.Menu.SoundMenu
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.instance
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.text.ParseException

object UtilityCommand: CommandExecutor, TabCompleter {
    
    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        if (cmd.name == "customencha") {
            val result = mutableListOf<String>()
            when (args.size) {
                1 -> {
                    CustomEnchantment.values().forEach { encha ->
                        if (encha.name.contains(args[0].toUpperCase())) result.add(encha.name)
                    }
                }
            }
            return result
        }
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
                    val enchant = when (args[0].toUpperCase()) {
                        "HOMING" -> CustomEnchantment.HOMING
                        "LIFE_STEAL" -> CustomEnchantment.LIFE_STEAL
                        "PROJECTILE_REFLECTION" -> CustomEnchantment.PROJECTILE_REFLECTION
                        "EXP_BOOST" -> CustomEnchantment.EXP_BOOST
                        "SHEARING" -> CustomEnchantment.SHEARING
                        "TELEKINESIS" -> CustomEnchantment.TELEKINESIS
                        else -> return true
                    }
                    try {
                        val level = if (args.size == 1) 1 else Integer.parseInt(args[1])
                        sender.inventory.setItemInMainHand(ItemExpansion(item).addEnchant(enchant, level).item)
                    } catch (e: ParseException) {}
                }
            }
        }
        return true
    }
}
