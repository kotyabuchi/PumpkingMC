package com.github.kotyabuchi.pumpkingmc.Enum

import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive.Parkour
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Defensive.Vitality
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.Archery
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.BattleAxe
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.SwordMaster
import com.github.kotyabuchi.pumpkingmc.Class.Comabt.Offensive.Unarmed
import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Class.Lifestyle.*
import org.bukkit.Material

enum class JobClassType(private val icon: Material, val regularName: String, val jobClass: JobClassMaster) {
    VITALITY(Material.IRON_CHESTPLATE, "Vitality", Vitality),
    PARKOUR(Material.LEATHER_BOOTS, "Parkour", Parkour),
    UNARMED(Material.IRON_BARS, "Unarmed", Unarmed),
    SWORDMASTER(Material.IRON_SWORD, "SwordMaster", SwordMaster),
    BATTLEAXE(Material.IRON_AXE, "BattleAxe", BattleAxe),
    ARCHERY(Material.BOW, "Bow", Archery),
    MINING(Material.DIAMOND_PICKAXE, "Mining", Mining),
    EXCAVATION(Material.IRON_SHOVEL, "Excavation", Excavation),
    WOODCUTTING(Material.STONE_AXE, "WoodCutting", Woodcutting),
    HERBALISM(Material.STONE_HOE, "Herbalism", Herbalism),
    BREEDING(Material.MILK_BUCKET, "Breeding", Breeding),
    BLACKSMITH(Material.SMITHING_TABLE, "BlackSmith", BlackSmith),
    REPAIR(Material.ANVIL, "Repair", Repair);

    fun getIcon(): Material = icon
}
