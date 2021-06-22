package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.System.Player.Home
import com.github.kotyabuchi.pumpkingmc.System.Player.getStatus
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.random.Random

class HomeButton(val home: Home): MenuButtonBase() {

    private val location = Location(home.world, home.x, home.y, home.z, home.yaw, 0f)

    init {
        val lore = mutableListOf<String>()
        lore.add("&fWorld: ${location.world?.name}".colorS())
        lore.add("&fX: ${location.x}".colorS())
        lore.add("&fY: ${location.y}".colorS())
        lore.add("&fZ: ${location.z}".colorS())
        lore.add("&fYaw: ${location.yaw}".colorS())
        lore.add("")
        lore.add("&6Left Click: Teleport to Location")
        lore.add("&6Right Click: Change icon")
        menuItem = ItemStackGenerator(home.icon).setDisplayName(("&f" + home.name).colorS()).setLore(lore).setMenuItemTag().generate()
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val world = player.world
        for (i in 0 until 20) {
            val x = Random.nextInt(15) / 10.0 - .75
            val y = Random.nextInt(20) / 10.0 - 1
            val z = Random.nextInt(15) / 10.0 - .75
            world.spawnParticle(Particle.SPELL, player.location.clone().add(.0, 1.0, .0).add(x, y, z), 20)
            world.spawnParticle(Particle.PORTAL, player.location.clone().add(.0, 1.0, .0).add(x, y, z), 20)
        }
        world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, .4f, .6f)
        player.getStatus().closeMenu()
        player.teleport(location)
        for (i in 0 until 20) {
            val x = Random.nextInt(15) / 10.0 - .75
            val y = Random.nextInt(20) / 10.0 - 1
            val z = Random.nextInt(15) / 10.0 - .75
            world.spawnParticle(Particle.SPELL, location.clone().add(.0, 1.0, .0).add(x, y, z), 20)
            world.spawnParticle(Particle.PORTAL, location.clone().add(.0, 1.0, .0).add(x, y, z), 20)
        }
        world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, .4f, .6f)
    }
}