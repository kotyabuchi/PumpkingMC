package com.github.kotyabuchi.pumpkingmc

import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive.Parkour
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive.Vitality
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.Archery
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.BattleAxe
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.SwordMaster
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.Unarmed
import com.github.kotyabuchi.pumpkingmc.Class.Lifestyle.*
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery.ArcShot
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery.GravityShot
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.Archery.StrongShot
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak.MultiBreakExcavation
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.MultiBreak.MultiBreakMining
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.SuperBreaker
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Woodcutting.TreeAssist
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Woodcutting.TreeFall
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster.BlinkStrike
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.SwordMaster.DoubleAttack
import com.github.kotyabuchi.pumpkingmc.Command.HomeCommand
import com.github.kotyabuchi.pumpkingmc.Command.SkillCommand
import com.github.kotyabuchi.pumpkingmc.Command.UtilityCommand
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantment
import com.github.kotyabuchi.pumpkingmc.CustomEnchantment.CustomEnchantmentManager
import com.github.kotyabuchi.pumpkingmc.CustomEvent.CustomEventCaller
import com.github.kotyabuchi.pumpkingmc.CustomItem.ItemManager
import com.github.kotyabuchi.pumpkingmc.CustomItem.TransportAmulet
import com.github.kotyabuchi.pumpkingmc.Entity.CustomEntity
import com.github.kotyabuchi.pumpkingmc.Entity.DebugStuff
import com.github.kotyabuchi.pumpkingmc.Entity.Friendly.EnchantedVillager
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.*
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.Zombie.EnchantedDrowned
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.Zombie.EnchantedHusk
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.Zombie.EnchantedPiglin
import com.github.kotyabuchi.pumpkingmc.Entity.Monster.Zombie.EnchantedZombie
import com.github.kotyabuchi.pumpkingmc.Menu.MenuController
import com.github.kotyabuchi.pumpkingmc.System.*
import com.github.kotyabuchi.pumpkingmc.System.Player.PlayerManager
import com.github.kotyabuchi.pumpkingmc.Utility.getServerVersion
import com.github.kotyabuchi.pumpkingmc.Utility.initDB
import com.github.kotyabuchi.pumpkingmc.Utility.savePlayerStatus
import com.github.kotyabuchi.pumpkingmc.Utility.startAutoSave
import org.bukkit.NamespacedKey
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

lateinit var instance: Main

class Main : JavaPlugin() {

    private val removeLag = RemoveLag()

    private lateinit var homeCommand: HomeCommand
    private val damagePopup = DamagePopup()

    private lateinit var zombieClasses: List<EnchantedZombie>
    
    private fun registerEvents() {
        val pm = server.pluginManager

        pm.registerEvents(PlayerManager(), this)

        // Command
        pm.registerEvents(homeCommand, this)

        pm.registerEvents(DebugStuff(), this)

        // CustomEnchantment
        pm.registerEvents(CustomEnchantmentManager, this)

        // Entity
            // Friendly
//        pm.registerEvents(AnimalExpansion(), this)
        pm.registerEvents(EnchantedVillager(), this)

            // Monster
        zombieClasses.forEach {
            pm.registerEvents(it, this)
        }
        pm.registerEvents(EnchantedCreeper(), this)
        pm.registerEvents(EnchantedEnderman(), this)
//        pm.registerEvents(EnchantedMagmaCube(), this)
        pm.registerEvents(EnchantedSkeleton(), this)
        pm.registerEvents(EnchantedSlime(), this)
        pm.registerEvents(EnchantedSpider(), this)
        pm.registerEvents(CustomEntity(), this)

        // CustomEvent
        pm.registerEvents(CustomEventCaller, this)

        // CustomItem
        pm.registerEvents(ItemManager, this)
        pm.registerEvents(TransportAmulet(), this)

        // Menu
        pm.registerEvents(MenuController(), this)

        // Skill
            // ActiveSkill
                // Archery
        pm.registerEvents(ArcShot, this)
        pm.registerEvents(GravityShot, this)
        pm.registerEvents(StrongShot, this)
                // BlockBreak
                    // MultiBreak
        pm.registerEvents(MultiBreakExcavation, this)
        pm.registerEvents(MultiBreakMining, this)
        pm.registerEvents(SuperBreaker, this)
                // SwordMaster
        pm.registerEvents(BlinkStrike, this)
        pm.registerEvents(DoubleAttack, this)
                // WoodCutting
        pm.registerEvents(TreeAssist, this)
        pm.registerEvents(TreeFall, this)
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
        pm.registerEvents(TombStone, this)
        pm.registerEvents(WorldGuard(), this)
    }
    
    private fun registerCommands() {
        this.getCommand("showsolidblock")?.setExecutor(UtilityCommand)
        this.getCommand("debugstuff")?.setExecutor(UtilityCommand)
        this.getCommand("soundlist")?.setExecutor(UtilityCommand)
        this.getCommand("allentity")?.setExecutor(UtilityCommand)
        this.getCommand("shownbti")?.setExecutor(UtilityCommand)
        this.getCommand("showencha")?.setExecutor(UtilityCommand)
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
        zombieClasses = listOf(EnchantedDrowned(), EnchantedHusk(), EnchantedPiglin(), EnchantedZombie())
        registerEvents()
        registerCommands()
        CustomEnchantment.registerEnchantment()
        clearBossBar()

//        removeLag.start()
        startAutoSave()

//        saveNewPlayerStatus()
        println("PumpkingFantasy Enabled in ${getServerVersion()}")
    }
    
    override fun onDisable() {
        // Plugin shutdown logic
        println("PumpkingFantasy Disabled")
        TombStone.saveTombStoneFile()
        savePlayerStatus()
        CustomEnchantment.unloadEnchantments()
        zombieClasses.forEach {
            it.clearStones()
        }
        clearBossBar()
        damagePopup.clearPopup()
        removeLag.stop()
    }

    fun registerEvent(listener: Listener) {
        server.pluginManager.registerEvents(listener, this)
    }

    fun callEvent(event: Event) {
        server.pluginManager.callEvent(event)
    }
}
