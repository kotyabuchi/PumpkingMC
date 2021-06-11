package com.github.kotyabuchi.pumpkingmc.CustomEnchantment

import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.ExpBoost
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.Combat.ProjectileReflection
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.Enchantments.Telekinesis
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.scheduler.BukkitRunnable

object CustomEnchantment {

    private val enchantments = mutableListOf<CustomEnchantmentMaster>()

    val TELEKINESIS = Telekinesis
    val PROJECTILE_REFLECTION = ProjectileReflection
    val EXP_BOOST = ExpBoost

    val EnchantmentKey = NamespacedKey(instance, "Enchantments")

    init {
        enchantments.add(TELEKINESIS)
        enchantments.add(PROJECTILE_REFLECTION)
        enchantments.add(EXP_BOOST)
    }

    fun registerEnchantment() {
        try {
            val field = Enchantment::class.java.getDeclaredField("acceptingNew")
            field.isAccessible = true
            field.set(null, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        enchantments.forEach {
            try {
                Enchantment.registerEnchantment(it)
                println("&aRegister Enchantment: ${it.key}".colorS())
            } catch (e: IllegalArgumentException) {
                println("&cRegistered Enchantment: ${it.key}".colorS())
            }
        }
    }

    fun unloadEnchantments(count: Int = 0) {
        if (count >= 10) return
        try {
            val keyField = Enchantment::class.java.getDeclaredField("byKey")

            keyField.isAccessible = true
            val byKey = keyField.get(null) as HashMap<*, *>

            enchantments.forEach {
                if (byKey.containsKey(it.key)) {
                    byKey.remove(it.key)
                }
            }

            val nameField = Enchantment::class.java.getDeclaredField("byName")

            nameField.isAccessible = true
            val byName = nameField.get(null) as HashMap<*, *>

            enchantments.forEach { enchant ->
                if (byName.containsKey(enchant.name)) {
                    byName.remove(enchant.name)
                }
            }

        } catch (e: NoClassDefFoundError) {
            println("Failed unload enchantment ($count times)")
            object : BukkitRunnable() {
                override fun run() {
                    unloadEnchantments(count + 1)
                }
            }.runTaskLater(instance, 10)
        }
    }

    fun values(): Array<CustomEnchantmentMaster> {
        return enchantments.toTypedArray()
    }
}