package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster

import com.destroystokyo.paper.ParticleBuilder
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ActiveSkillMaster
import com.github.kotyabuchi.pumpkingmc.Utility.sendActionMessage
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.round

object BlinkStrike: ActiveSkillMaster {
    override val skillName: String = "BlinkStrike"
    override val cost: Int = 0
    override val coolTime: Long = 0
    override val needLevel: Int = 100
    override var description: String = "一定距離をブリンクし、範囲内の敵を攻撃する"
    override val hasActiveTime: Boolean = false
    override val activeTimeMap: MutableMap<UUID, BukkitTask> = mutableMapOf()
    override val lastUseTime: MutableMap<UUID, Long> = mutableMapOf()
    override fun calcActiveTime(level: Int): Int = 0

    private val transparentBlocks = mutableSetOf<Material>()

    init {
        Material.values().forEach {
            if (it.isBlock && !it.isSolid && !it.isOccluding) transparentBlocks.add(it)
        }
    }

    override fun enableAction(player: Player, level: Int) {
        player.world.playSound(player.eyeLocation, Sound.BLOCK_BEACON_ACTIVATE, .4f, 2f)
        player.sendActionMessage("&e$skillName ready")
    }

    override fun disableAction(player: Player) {
    }

    @EventHandler
    fun onSwingSword(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (!event.action.name.startsWith("LEFT_CLICK_")) return
        val player = event.player
        val playerLoc = player.location.clone()
        val dire = player.eyeLocation.direction.normalize().multiply(.5)

        val item = player.inventory.itemInMainHand
        if (!item.type.name.endsWith("_SWORD")) return

        if (isEnabledSkill(player)) {
            disableSkill(player)

            val targetBlock = player.getTargetBlock(transparentBlocks, 20)

            val face = player.getTargetBlockFace(20)
            val loc = if (face == null) {
                targetBlock.location.add(.5, .0, .5)
            } else {
                targetBlock.getRelative(face).location.add(.5, .0, .5)
            }
            loc.pitch = playerLoc.pitch
            loc.yaw = playerLoc.yaw
            player.teleport(loc)
            player.world.playSound(playerLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.3f)
            player.world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.3f)
            object : BukkitRunnable() {
                override fun run() {
                    player.velocity = dire.clone().multiply(2)
                }
            }.runTaskLater(instance, 0)

            val targetEntities = mutableSetOf<LivingEntity>()

            val effectLoc = mutableListOf(playerLoc.add(.0, 1.25, .0))
            val repeatAmount = round(playerLoc.distance(loc) * 2).toInt()
            repeat(repeatAmount) {
                effectLoc.add(playerLoc.add(dire).clone())
                targetEntities.addAll(playerLoc.getNearbyLivingEntities(.75))
            }

            val removeSet = mutableSetOf<LivingEntity>()
            targetEntities.forEach {
                if (it is ItemFrame || it is Item || it == player) {
                    removeSet.add(it)
                } else {
                    it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 5, 100, true, false))
                    it.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 5, 100, true, false))
                }
            }
            targetEntities.removeAll(removeSet)

            val particleBuilder = ParticleBuilder(Particle.SWEEP_ATTACK)
            particleBuilder.location(effectLoc.first())
            particleBuilder.receivers(16 * 3)
            particleBuilder.count(2)
            particleBuilder.offset(.2, .2, .2)

            var damage = when (item.type) {
                Material.WOODEN_SWORD, Material.GOLDEN_SWORD -> 4.0
                Material.STONE_SWORD -> 5.0
                Material.IRON_SWORD -> 6.0
                Material.DIAMOND_SWORD -> 7.0
                Material.NETHERITE_SWORD -> 8.0
                else -> 0.0
            }
            if (item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                damage += (.5 * item.getEnchantmentLevel(Enchantment.DAMAGE_ALL) + .5)
            }
            val fireLevel = item.getEnchantmentLevel(Enchantment.FIRE_ASPECT)

            object : BukkitRunnable() {
                override fun run() {
                    effectLoc.forEachIndexed { index, location ->
                        particleBuilder.location(location)
                        particleBuilder.spawn()
                        if (index % 2 == 0) player.world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, .5f, 1.3f)
                    }
                    targetEntities.forEach {
                        it.damage(damage, player)
                        it.fireTicks += fireLevel * 80
                    }
                }
            }.runTaskLater(instance, 5)
        }
    }
}