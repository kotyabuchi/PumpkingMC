package com.github.kotyabuchi.pumpkingmc.Entity.Monster

import com.github.kotyabuchi.pumpkingmc.Utility.isLiquid
import com.github.kotyabuchi.pumpkingmc.Utility.jump
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import kotlin.random.Random

class EnchantedSkeleton: MobExpansionMaster(EntityType.SKELETON) {

    private val weaponMap = mutableMapOf<Mob, ItemStack>()
    private val swapCoolDown = mutableMapOf<Mob, Long>()
    private val playerVecMap = mutableMapOf<Player, Vector>()
    private val coolDown: MutableMap<Mob, MutableMap<SkeletonAction, Long>> = mutableMapOf()

    init {
        addSpawnAction { skeleton ->
            skeleton.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = (skeleton.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.value ?: 0.22) + (Random.nextInt(10) / 100.0)
            skeleton.equipment?.let {
                if (it.helmet?.type == Material.AIR) {
                    val helmet = ItemStack(Material.LEATHER_HELMET)
                    val meta = helmet.itemMeta as LeatherArmorMeta
                    meta.setColor(Color.fromRGB(236, 240, 241))
                    helmet.itemMeta = meta
                    it.helmet = helmet
                    it.helmetDropChance = 0f
                }
                if (Random.nextInt(100) < 10) {
                    it.setItemInOffHand(ItemStack(Material.SHIELD))
                    it.itemInOffHandDropChance = 0f
                }
            }
        }
        addInFightAction { skeleton, target ->
            if (skeleton.location.distance(target.location) <= 4.5 && !skeleton.location.block.type.isLiquid()) {
                meleeAction(skeleton)
            } else if (skeleton.location.block.type.isLiquid() || weaponMap.containsKey(skeleton) && skeleton.location.distance(target.location) >= 5) {
                rangedAction(skeleton)
            }
        }
        addEndFightAction { skeleton, _ ->
            weaponMap[skeleton]?.let {
                skeleton.equipment?.setItemInMainHand(it)
            }
            weaponMap.remove(skeleton)
            coolDown.remove(skeleton)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.to
        val vec = to.toVector().subtract(event.from.toVector())
        playerVecMap[player] = vec
    }
    
    @EventHandler
    fun onShoot(event: EntityShootBowEvent) {
        val skeleton = event.entity as? Skeleton ?: return
        val arrow = event.projectile as? Arrow ?: return
        val player = skeleton.target as? Player ?: return
        val playerLoc = player.location
        val distance = skeleton.location.distance(playerLoc)
        val hitTime = distance * 25
        val deviation = (playerVecMap[player] ?: player.velocity).multiply(hitTime / 50.0).setY((playerVecMap[player]?.y ?: 0.1) / 5)

        arrow.velocity = playerLoc.add(.0, 1.5, .0).add(deviation).subtract(skeleton.location.add(.0, 1.2, .0)).toVector().normalize().multiply(2)

        startRunnable(skeleton)
    }

    @EventHandler
    fun onBlockArrow(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val arrow = event.damager as? Arrow ?: return
        val skeleton = arrow.shooter as? Skeleton ?: return
        val playerLoc = player.location
        val distance = skeleton.location.distance(playerLoc)
        val hitTime = distance * 25
        val deviation = (playerVecMap[player] ?: player.velocity).multiply(hitTime / 50.0).setY((playerVecMap[player]?.y ?: 0.1) / 5)

        if (player.isBlocking || player.isHandRaised) {
            meleeAction(skeleton)
            skeleton.jump(playerLoc.add(deviation))
        }
    }

    private fun rangedAction(skeleton: Mob, forceChange: Boolean = false) {
        val pdc = skeleton.persistentDataContainer
        if (pdc.has(NamespacedKey(instance, "Melee"), PersistentDataType.BYTE)) {
            if (forceChange || canSwapWeapon(skeleton)) {
                val equipment = skeleton.equipment ?: return
                weaponMap[skeleton]?.let {
                    equipment.setItemInMainHand(it)
                }
                weaponMap.remove(skeleton)
                swapCoolDown[skeleton] = System.currentTimeMillis()
                pdc.remove(NamespacedKey(instance, "Melee"))
            } else {
                meleeAction(skeleton)
                return
            }
        }
    }

    private fun meleeAction(skeleton: Mob) {
        val target = skeleton.target ?: return
        val loc = skeleton.location
        if (loc.block.type.isLiquid()) {
            rangedAction(skeleton, true)
            return
        }
        val pdc = skeleton.persistentDataContainer
        if (!pdc.has(NamespacedKey(instance, "Melee"), PersistentDataType.BYTE)) {
            if (canSwapWeapon(skeleton)) {
                val equipment = skeleton.equipment ?: return
                if (equipment.itemInMainHand.type == Material.BOW) {
                    weaponMap[skeleton] = equipment.itemInMainHand.clone()
                    equipment.setItemInMainHand(ItemStack(Material.STONE_AXE))
                }
                setLastAttackTime(skeleton, System.currentTimeMillis())
                swapCoolDown[skeleton] = System.currentTimeMillis()
                pdc.set(NamespacedKey(instance, "Melee"), PersistentDataType.BYTE, 1)
            } else {
                rangedAction(skeleton)
                return
            }
        }
        when (Random.nextInt(10)) {
            0,1 -> {
                if (canDoAction(skeleton, SkeletonAction.JUMP_FORWARD)) {
                    skeleton.jump(target.location)
                    setLastAttackTime(skeleton, 0)
                    setCoolDown(skeleton, SkeletonAction.JUMP_FORWARD, System.currentTimeMillis())
                }
            }
            2,3,4 -> {
                if (canDoAction(skeleton, SkeletonAction.BACK_STEP)) {
                    val vec = skeleton.location.toVector().subtract(target.location.toVector()).normalize().multiply(1.5).setY(.5)
                    skeleton.velocity = vec
                    setCoolDown(skeleton, SkeletonAction.BACK_STEP, System.currentTimeMillis())
                    rangedAction(skeleton, true)
                }
            }
            else -> {
                if (System.currentTimeMillis() - getLastAttackTime(skeleton) >= 2500 && canDoAction(skeleton, SkeletonAction.BACK_STEP)) {
                    val vec = skeleton.location.toVector().subtract(target.location.toVector()).normalize().multiply(1.5).setY(.5)
                    skeleton.velocity = vec
                    setLastAttackTime(skeleton, System.currentTimeMillis())
                    setCoolDown(skeleton, SkeletonAction.BACK_STEP, System.currentTimeMillis())
                    rangedAction(skeleton, true)
                }
            }
        }
    }

    private fun setCoolDown(mob: Mob, action: SkeletonAction, time: Long) {
        val map = coolDown[mob] ?: mutableMapOf()
        map[action] = time
    }

    private fun canDoAction(mob: Mob, action: SkeletonAction): Boolean {
        val map = coolDown[mob] ?: return true
        return System.currentTimeMillis() - (map[action] ?: 0) >= action.coolDown
    }

    private fun canSwapWeapon(mob: Mob): Boolean {
        return (System.currentTimeMillis() - (swapCoolDown[mob] ?: 0)) >= 1000
    }

    enum class SkeletonAction(val coolDown: Int) {
        JUMP_FORWARD(1500),
        BACK_STEP(2000)
    }
}
