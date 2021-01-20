package com.github.kotyabuchi.pumpkingmc

import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive.Parkour
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive.Vitality
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.Archery
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.BattleAxe
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.SwordMaster
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.Unarmed
import com.github.kotyabuchi.pumpkingmc.Class.Lifestyle.*
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.Command.HomeCommand
import com.github.kotyabuchi.pumpkingmc.Command.SkillCommand
import com.github.kotyabuchi.pumpkingmc.Command.UtilityCommand
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantment
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentManager
import com.github.kotyabuchi.pumpkingmc.CustomEvent.CustomEventCaller
import com.github.kotyabuchi.pumpkingmc.CustomItem.TransportAmulet
import com.github.kotyabuchi.pumpkingmc.Entity.CustomEntity
import com.github.kotyabuchi.pumpkingmc.Entity.DebugStuff
import com.github.kotyabuchi.pumpkingmc.Entity.Friendly.EnchantedVillager
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.*
import com.github.kotyabuchi.pumpkingmc.Menu.MenuController
import com.github.kotyabuchi.pumpkingmc.System.*
import com.github.kotyabuchi.pumpkingmc.System.Player.PlayerManager
import com.github.kotyabuchi.pumpkingmc.Utility.initDB
import com.github.kotyabuchi.pumpkingmc.Utility.savePlayerStatus
import com.github.kotyabuchi.pumpkingmc.Utility.startAutoSave
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

lateinit var instance: Main

class Main : JavaPlugin() {

    private val removeLag = RemoveLag()

    private lateinit var homeCommand: HomeCommand
    private val damagePopup = DamagePopup()
    
    private fun registerEvents() {
        val pm = server.pluginManager

        pm.registerEvents(PlayerManager(), this)

        // Command
        pm.registerEvents(homeCommand, this)

        pm.registerEvents(DebugStuff(), this)

        // CustomEnchantment
        pm.registerEvents(CustomEnchantmentManager(), this)

        // Entity
            // Friendly
//        pm.registerEvents(AnimalExpansion(), this)
        pm.registerEvents(EnchantedVillager(), this)

            // Monster
        pm.registerEvents(EnchantedDrowned(), this)
        pm.registerEvents(EnchantedCreeper(), this)
        pm.registerEvents(EnchantedEnderman(), this)
        pm.registerEvents(EnchantedHusk(), this)
//        pm.registerEvents(EnchantedMagmaCube(), this)
        pm.registerEvents(EnchantedPiglin(), this)
        pm.registerEvents(EnchantedSkeleton(), this)
        pm.registerEvents(EnchantedSlime(), this)
        pm.registerEvents(EnchantedSpider(), this)
        pm.registerEvents(EnchantedZombie(), this)
        pm.registerEvents(CustomEntity(), this)

        // CustomEvent
        pm.registerEvents(CustomEventCaller(), this)

        // CustomItem
        pm.registerEvents(TransportAmulet(), this)

        // Menu
        pm.registerEvents(MenuController(), this)

        // Skill
            // ActiveSkill
        pm.registerEvents(SuperBreaker, this)
            // ClassSkill
                // CombatSkill
                    // Defensive
        pm.registerEvents(Parkour, this)
        pm.registerEvents(Vitality, this)
                    // Offensive
        pm.registerEvents(Archery, this)
        pm.registerEvents(BattleAxe, this)
        pm.registerEvents(SwordMaster, this)
        pm.registerEvents(Unarmed, this)
                // LifestyleSkill
        pm.registerEvents(BlackSmith, this)
        pm.registerEvents(Breeding, this)
        pm.registerEvents(Excavation,this)
        pm.registerEvents(Herbalism, this)
        pm.registerEvents(Mining, this)
        pm.registerEvents(Repair, this)
        pm.registerEvents(Woodcutting, this)

        // System
            // Player

//        pm.registerEvents(CartExpansion(), this)
        pm.registerEvents(CustomDurability(), this)
        pm.registerEvents(damagePopup, this)
        pm.registerEvents(ItemExpansionManager(), this)
        pm.registerEvents(LeadExpansion(), this)
        pm.registerEvents(OtherSystem(), this)
        pm.registerEvents(ShowMobHealth(), this)
        pm.registerEvents(WorldGuard(), this)
    }
    
    private fun registerCommands() {
        this.getCommand("showsolidblock")?.setExecutor(UtilityCommand)
        this.getCommand("debugstuff")?.setExecutor(UtilityCommand)
        this.getCommand("soundlist")?.setExecutor(UtilityCommand)
        this.getCommand("allentity")?.setExecutor(UtilityCommand)
        this.getCommand("shownbti")?.setExecutor(UtilityCommand)
        this.getCommand("customencha")?.setExecutor(UtilityCommand)
        this.getCommand("skill")?.setExecutor(SkillCommand())
        this.getCommand("home")?.setExecutor(homeCommand)
    }
    
    private fun clearBossBar() {
        val removeList = mutableListOf<NamespacedKey>()
        server.bossBars.forEach {
            it.removeAll()
            removeList.add(it.key)
        }
        removeList.forEach {
            server.removeBossBar(it)
        }
    }
    
    override fun onEnable() {
        // Plugin startup logic
        println("PumpkingFantasy Enabling")
        instance = this
        if (!dataFolder.exists()) dataFolder.mkdirs()
        initDB()
        homeCommand = HomeCommand()
        registerEvents()
        registerCommands()
        CustomEnchantment.registerEnchantment()
        clearBossBar()

        removeLag.start()
        startAutoSave()

//        saveNewPlayerStatus()
        println("PumpkingFantasy Enabled")
    }
    
    override fun onDisable() {
        // Plugin shutdown logic
        println("PumpkingFantasy Disabled")
        savePlayerStatus()
        CustomEnchantment.unloadEnchantments()
        clearBossBar()
        damagePopup.clearPopup()
        removeLag.stop()
    }
}
