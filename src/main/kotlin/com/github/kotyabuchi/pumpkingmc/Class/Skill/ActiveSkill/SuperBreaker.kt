package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.floor
import kotlin.math.max

object SuperBreaker: ToolLinkedSkill {
    override val skillName: String = "SuperBreaker"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 25
    override val description: String = ""
    override val hasActiveTime: Boolean = true
    override val activePlayers: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val coolTimePlayers: MutableList<UUID> = mutableListOf()
    override val skillItemBackup: MutableMap<UUID, ItemStack> = mutableMapOf()
    override fun calcActiveTime(level: Int): Int = 40 + level / 25 * 30

    private fun getBossBarKey(player: Player): NamespacedKey = NamespacedKey(instance, skillName + "_" + player.uniqueId.toString())

    override fun enableAction(player: Player, level: Int) {
        val bossBarKey = getBossBarKey(player)
        val item = player.inventory.itemInMainHand.clone()
        val backup = item.clone()
        val digSpeedLevel = item.getEnchantmentLevel(Enchantment.DIG_SPEED) + 5
        val meta = item.itemMeta ?: return

        meta.removeEnchant(Enchantment.DIG_SPEED)
        meta.addEnchant(Enchantment.DIG_SPEED, digSpeedLevel, true)
        item.itemMeta = meta
        player.inventory.setItemInMainHand(item)

        val bossBar = instance.server.getBossBar(bossBarKey) ?: instance.server.createBossBar(bossBarKey, ChatColor.GOLD.toString() + "Super Breaker", BarColor.YELLOW, BarStyle.SEGMENTED_10)
        bossBar.progress = 1.0
        bossBar.addPlayer(player)
        bossBar.isVisible = true

        var count = calcActiveTime(level)
        val oneTimeProgress = 1.0 / count
        object : BukkitRunnable() {
            override fun run() {
                if (instance.server.getBossBar(bossBarKey) == null) {
                    cancel()
                } else {
                    bossBar.setTitle(ChatColor.GOLD.toString() + "Super Breaker (" + floor(count / 2.0) / 10 + ")")
                    bossBar.progress = max(bossBar.progress - oneTimeProgress, 0.0)
                    count--
                    if (count <= 0) {
                        bossBar.removeAll()
                        instance.server.removeBossBar(bossBarKey)
                        cancel()
                    }
                }
            }
        }.runTaskTimer(instance, 0, 1)
        skillItemBackup[player.uniqueId] = backup
        player.sendActionBar('&', "&a$skillName Enabled")
    }

    override fun disableAction(player: Player) {
        val uuid = player.uniqueId
        val bossBarKey = getBossBarKey(player)
        instance.server.getBossBar(bossBarKey)?.isVisible = false

        if (!player.inventory.itemInMainHand.type.isAir) {
            skillItemBackup[uuid]?.let { item ->
                if (player.inventory.itemInMainHand.itemMeta !is Damageable) {
                    player.server.getPlayer("kabocchi")?.let {
                        it.sendMessage("Not Damageable Error")
                        it.sendMessage(player.name)
                        it.sendMessage(player.inventory.itemInMainHand.type.name)
                    }
                }
                val backupItem = ItemExpansion(item)
                backupItem.setDurability(ItemExpansion(player.inventory.itemInMainHand).getDurability())
                player.inventory.setItemInMainHand(backupItem.item)
            }
        }
        skillItemBackup.remove(uuid)
        player.sendActionBar('&', "&c$skillName Disabled")
    }
}
