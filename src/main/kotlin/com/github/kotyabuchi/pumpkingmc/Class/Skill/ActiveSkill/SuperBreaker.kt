package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.floor
import kotlin.math.max

class SuperBreaker: Listener {
    private val superBreakers = mutableMapOf<Player, BukkitTask>()
    private val superBreakerItemBackup = mutableMapOf<Player, ItemStack>()

    fun isSuperBreaking(player: Player): Boolean = superBreakers.containsKey(player)

    fun enableSuperBreaker(player: Player, jobClassType: JobClassType) {
        val level = player.getStatus().getJobClassStatus(jobClassType).getLevel()
        val bossBarKey = NamespacedKey(instance, "SuperBreaker_" + player.uniqueId.toString())
        if (level < 10) {
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f)
            player.sendActionBar('&', "&cNot enough levels (Need Lv.10)")
            return
        }
        if (isSuperBreaking(player)) {
            disableSuperBreaker(player)
            return
        }
        val item = player.inventory.itemInMainHand.clone()
        val backup = item.clone()
        val digSpeedLevel = item.getEnchantmentLevel(Enchantment.DIG_SPEED) + 5
        val meta = item.itemMeta ?: return
        meta.removeEnchant(Enchantment.DIG_SPEED)
        meta.addEnchant(Enchantment.DIG_SPEED, digSpeedLevel, true)
        item.itemMeta = meta
        player.inventory.setItemInMainHand(item)
        player.sendActionBar('&', "&aSuper Breaker Enabled")
        val bossBar = instance.server.getBossBar(bossBarKey) ?: instance.server.createBossBar(bossBarKey, ChatColor.GOLD.toString() + "Super Breaker", BarColor.YELLOW, BarStyle.SEGMENTED_10)
        bossBar.progress = 1.0
        bossBar.addPlayer(player)
        bossBar.isVisible = true
    
        var count = 40 + level / 25 * 30
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
        superBreakerItemBackup[player] = backup
        superBreakers[player] = object : BukkitRunnable() {
            override fun run() {
                disableSuperBreaker(player)
            }
        }.runTaskLater(instance, count.toLong())
    }

    @EventHandler
    fun onClickItem(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!isSuperBreaking(player)) return
        if (event.clickedInventory == null) return
        if (event.slot != player.inventory.heldItemSlot) return
        disableSuperBreaker(player, true)
        event.isCancelled = true
    }

    @EventHandler
    fun onBreakItem(event: PlayerItemBreakEvent) {
        val player = event.player
        if (isSuperBreaking(player)) {
            disableSuperBreaker(player, false)
        }
    }
    
    @EventHandler
    fun onChangeItemSlot(event: PlayerItemHeldEvent) {
        val player = event.player
        if (isSuperBreaking(player)) {
            disableSuperBreaker(player)
        }
    }
    
    @EventHandler
    fun onDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        if (isSuperBreaking(player)) {
            player.inventory.setItemInMainHand(event.itemDrop.itemStack)
            event.itemDrop.remove()
            disableSuperBreaker(player)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (isSuperBreaking(player)) {
            disableSuperBreaker(player)
        }
    }
    
    @EventHandler
    fun onLogout(event: PlayerQuitEvent) {
        val player = event.player
        if (isSuperBreaking(player)) {
            disableSuperBreaker(player)
        }
    }
    
    private fun disableSuperBreaker(player: Player, salvageItem: Boolean = true) {
        val bossBarKey = NamespacedKey(instance, "SuperBreaker_" + player.uniqueId.toString())
        superBreakers[player]?.cancel()
        superBreakers.remove(player)
        instance.server.getBossBar(bossBarKey)?.isVisible = false
        if (salvageItem && !player.inventory.itemInMainHand.type.isAir) {
            if (player.inventory.itemInMainHand.itemMeta !is Damageable) {
                player.server.getPlayer("kabocchi")?.let {
                    it.sendMessage("Not Damageable Error")
                    it.sendMessage(player.name)
                    it.sendMessage(player.inventory.itemInMainHand.type.name)
                }
            }
            superBreakerItemBackup[player]?.let {
                val backupItem = ItemExpansion(it)
                backupItem.setDurability(ItemExpansion(player.inventory.itemInMainHand).getDurability())
                superBreakerItemBackup[player] = backupItem.item
            }
            player.inventory.setItemInMainHand(superBreakerItemBackup[player]!!)
        }
        superBreakerItemBackup.remove(player)
        player.sendActionBar('&', "&cSuper Breaker Disabled")
    }
}
