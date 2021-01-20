package com.github.kotyabuchi.pumpkingmc.Command

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.Menu.SkillMenu
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.text.ParseException

class SkillCommand: CommandExecutor, TabCompleter {
    
    private val args1Commands = mutableListOf("show")
    private val args1OpCommands = mutableListOf("set", "add", "reset", "load")
    private val skillNames = mutableListOf<String>()
    private val args3Commands = mutableListOf("exp", "level")
    
    init {
        JobClassType.values().forEach {
            skillNames.add(it.name)
        }
    }
    
    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        when (args.size) {
            1 -> {
                val result = mutableListOf<String>()
                args1Commands.forEach {
                    if (it.startsWith(args[0].toLowerCase())) result.add(it)
                }
                if (sender.isOp) {
                    args1OpCommands.forEach {
                        if (it.startsWith(args[0].toLowerCase())) result.add(it)
                    }
                }
                instance.server.onlinePlayers.forEach {
                    if (it.name.toUpperCase().startsWith(args[0].toUpperCase())) result.add(it.name)
                }
                return result
            }
            2 -> {
                val result = mutableListOf<String>()
                skillNames.forEach {
                    if (it.startsWith(args[1].toUpperCase())) result.add(it)
                }
                return result
            }
            3 -> {
                val result = mutableListOf<String>()
                if (args[0] != "show") {
                    args3Commands.forEach {
                        if (it.startsWith(args[2].toLowerCase())) result.add(it)
                    }
                    return result
                }
            }
            5 -> {
                val result = mutableListOf<String>()
                instance.server.onlinePlayers.forEach {
                    if (it.name.toUpperCase().startsWith(args[4].toUpperCase())) result.add(it.name)
                }
                return result
            }
        }
        return instance.onTabComplete(sender, cmd, label, args) ?: mutableListOf()
    }
    
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        if (args.isEmpty() || (args.size == 1 && instance.server.getPlayer(args[0]) != null)) {
            val target = if (args.size == 1) instance.server.getPlayer(args[0]) ?: sender else sender
            sender.getStatus().openMenu(SkillMenu(target))
        } else {
            when (args[0]) {
                "show" -> {
                    when (args.size) {
                        1 -> {
                            JobClassType.values().forEach {
                                val jobClassStatus = sender.getStatus().getJobClassStatus(it)
                                val stringBuilder = StringBuilder()
                                stringBuilder.append("=+-------------------------+=\n")
                                stringBuilder.append(it.regularName + "\n")
                                stringBuilder.append("Level: ${jobClassStatus.getLevel()}\n")
                                stringBuilder.append("Exp: ${jobClassStatus.getExp()}\n")
                                stringBuilder.append("TotalExp: ${jobClassStatus.getTotalExp()}\n")
                                stringBuilder.append("NextLevelExp: ${jobClassStatus.getNextLevelExp()}\n")
                                stringBuilder.append("=+-------------------------+=")
                                sender.sendMessage(stringBuilder.toString())
                            }
                        }
                        2 -> {
                            if (checkExistSkill(args[1])) {
                                val jobClassStatus = sender.getStatus().getJobClassStatus(JobClassType.valueOf(args[1]))
                                val stringBuilder = StringBuilder()
                                stringBuilder.append("=+-------------------------+=\n")
                                stringBuilder.append(args[1].toUpperCase() + "\n")
                                stringBuilder.append("Level: ${jobClassStatus.getLevel()}\n")
                                stringBuilder.append("Exp: ${jobClassStatus.getExp()}\n")
                                stringBuilder.append("TotalExp: ${jobClassStatus.getTotalExp()}\n")
                                stringBuilder.append("NextLevelExp: ${jobClassStatus.getNextLevelExp()}\n")
                                stringBuilder.append("=+-------------------------+=")
                                sender.sendMessage(stringBuilder.toString())
                            } else {
                                sender.sendMessage(ChatColor.RED.toString() + "スキル[${args[1]}]は存在しません")
                            }
                        }
                        3 -> {
                            if (checkExistSkill(args[1])) {
                                val targetPlayer = instance.server.getPlayer(args[2]) ?: return true
                                val jobClassStatus = targetPlayer.getStatus().getJobClassStatus(JobClassType.valueOf(args[1]))
                                val stringBuilder = StringBuilder()
                                stringBuilder.append("=+-------------------------+=\n")
                                stringBuilder.append(args[1].toUpperCase() + "\n")
                                stringBuilder.append("Level: ${jobClassStatus.getLevel()}\n")
                                stringBuilder.append("Exp: ${jobClassStatus.getExp()}\n")
                                stringBuilder.append("TotalExp: ${jobClassStatus.getTotalExp()}\n")
                                stringBuilder.append("NextLevelExp: ${jobClassStatus.getNextLevelExp()}\n")
                                stringBuilder.append("=+-------------------------+=")
                                sender.sendMessage(stringBuilder.toString())
                            } else {
                                sender.sendMessage(ChatColor.RED.toString() + "スキル[${args[1]}]は存在しません")
                            }
                        }
                    }
                }
                "set" -> {
                    if (!sender.isOp) return true
                    if (args.size < 4) return true
                    if (checkExistSkill(args[1])) {
                        val target = if (args.size == 5) instance.server.getPlayer(args[4]) ?: return true else sender
                        val skill = JobClassType.valueOf(args[1])
                        val playerStatus = target.getStatus()
                        val jobClassStatus = playerStatus.getJobClassStatus(skill)
                        when (args[2]) {
                            "exp" -> {
                                try {
                                    jobClassStatus.setExp(args[3].toDouble())
                                    sender.sendMessage("${args[1].toUpperCase()} exp set to ${args[3]}")
                                } catch (e: ParseException) {
                                    sender.sendMessage(ChatColor.RED.toString() + args[3] + "は数字ではありません")
                                }
                            }
                            "level" -> {
                                try {
                                    jobClassStatus.setLevel(Integer.parseInt(args[3]))
                                    sender.sendMessage("${args[1].toUpperCase()} level set to ${args[3]}")
                                } catch (e: ParseException) {
                                    sender.sendMessage(ChatColor.RED.toString() + args[3] + "は数字ではありません")
                                }
                            }
                        }
                        playerStatus.setJobClassStatus(skill, jobClassStatus)
                    } else {
                        sender.sendMessage(ChatColor.RED.toString() + "スキル[${args[1]}]は存在しません")
                    }
                }
                "add" -> {
                    if (!sender.isOp) return true
                    if (args.size < 4) return true
                    if (checkExistSkill(args[1])) {
                        val target = if (args.size == 5) instance.server.getPlayer(args[4]) ?: return true else sender
                        val skill = JobClassType.valueOf(args[1])
                        val playerStatus = target.getStatus()
                        val jobClassStatus = playerStatus.getJobClassStatus(skill)
                        when (args[2]) {
                            "exp" -> {
                                try {
                                    jobClassStatus.addExp(args[3].toDouble())
                                    sender.sendMessage("${args[3]} exp add to ${args[1].toUpperCase()}")
                                } catch (e: ParseException) {
                                    sender.sendMessage(ChatColor.RED.toString() + args[3] + "は数字ではありません")
                                }
                            }
                            "level" -> {
                                try {
                                    jobClassStatus.addLevel(Integer.parseInt(args[3]))
                                    sender.sendMessage("${args[3]} level add to ${args[1].toUpperCase()}")
                                } catch (e: ParseException) {
                                    sender.sendMessage(ChatColor.RED.toString() + args[3] + "は数字ではありません")
                                }
                            }
                        }
                        playerStatus.setJobClassStatus(skill, jobClassStatus)
                    } else {
                        sender.sendMessage(ChatColor.RED.toString() + "スキル[${args[1]}]は存在しません")
                    }
                }
                "reset" -> {
                    if (!sender.isOp) return true
                    when (args.size) {
                        2 -> {
                            if (checkExistSkill(args[1])) {
                                sender.getStatus().getJobClassStatus(JobClassType.valueOf(args[1])).reset()
                                sender.sendMessage("スキル[${args[1]}]をリセットしました")
                            } else {
                                sender.sendMessage(ChatColor.RED.toString() + "スキル[${args[1]}]は存在しません")
                            }
                        }
                        3 -> {
                            if (checkExistSkill(args[1])) {
                                val targetPlayer = instance.server.getPlayer(args[2]) ?: return true
                                targetPlayer.getStatus().getJobClassStatus(JobClassType.valueOf(args[1])).reset()
                                sender.sendMessage("${targetPlayer}さんのスキル[${args[1]}]をリセットしました")
                            } else {
                                sender.sendMessage(ChatColor.RED.toString() + "スキル[${args[1]}]は存在しません")
                            }
                        }
                    }
                }
                "load" -> {
                    if (!sender.isOp) return true
                    val target = if (args.size == 2) instance.server.getPlayer(args[1]) ?: sender else sender
                    val playerStatus = target.getStatus()
                    val pdc = target.persistentDataContainer
                    JobClassType.values().forEach { skill ->
                        val jobClassStatus = playerStatus.getJobClassStatus(skill)
                        val expKey = NamespacedKey(instance, skill.name + "_Exp")
                        val levelKey = NamespacedKey(instance, skill.name + "_Level")
                        val totalExpKey = NamespacedKey(instance, skill.name + "_TotalExp")
                        val exp = pdc.get(expKey, PersistentDataType.DOUBLE)
                        val level = pdc.get(levelKey, PersistentDataType.INTEGER)
                        exp?.let { jobClassStatus.setExp(it) }
                        level?.let { jobClassStatus.setLevel(it) }
                        pdc.remove(expKey)
                        pdc.remove(levelKey)
                        pdc.remove(totalExpKey)
                        playerStatus.setJobClassStatus(skill, jobClassStatus)
                    }
                }
            }
        }
        return true
    }
    
    private fun checkExistSkill(skillName: String): Boolean {
        return try {
            JobClassType.valueOf(skillName.toUpperCase())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
