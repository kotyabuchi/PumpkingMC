package com.github.kotyabuchi.pumpkingmc.Enum

import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

enum class JobClassType(private val icon: Material, val regularName: String, val commands: List<String>) {
    VITALITY(Material.IRON_CHESTPLATE, "Vitality", listOf()),
    PARKOUR(Material.LEATHER_BOOTS, "Parkour", listOf()),
    UNARMED(Material.IRON_BARS, "Unarmed", listOf()),
    SWORDMASTER(Material.IRON_SWORD, "SwordMaster", listOf(
        "RRR: Double Attack (100Lv)",
        "次の攻撃が2回攻撃になる"
    )),
    BATTLEAXE(Material.IRON_AXE, "BattleAxe", listOf()),
    ARCHERY(Material.BOW, "Bow", listOf(
        "RRR: Strong Shot (50Lv)",
        "次の矢の速度が上昇する",
        "",
        "LLL: Arc Shot (400Lv)",
        "範囲内に矢の雨を降らせる",
        "",
        "LRL: Gravity Shot (200Lv)",
        "着弾点に引き寄せられる矢を射る"
    )),
    MINING(Material.DIAMOND_PICKAXE, "Mining", listOf(
        "RRR: Super Breaker (25Lv)",
        "一定時間採掘速度が上昇する",
        "",
        "LLL: Stone Replacer (50Lv)",
        "ピッケルの右のスロットに石・丸石を入れておくと、\n鉱石を破壊した際に石と置き換わる",
        "",
        "LRL: Mine Assist (50Lv)",
        "鉱石を破壊した際につながった鉱石も一括で破壊する",
        "",
        "LLR: Multi Breaker (100Lv)",
        "破壊したブロックを中心に3x3の範囲を一括で破壊する"
    )),
    EXCAVATION(Material.IRON_SHOVEL, "Excavation", listOf(
        "RRR: Super Breaker (25Lv)",
        "一定時間採掘速度が上昇する"
    )),
    WOODCUTTING(Material.STONE_AXE, "WoodCutting", listOf(
        "RRR: Super Breaker (25Lv)",
        "一定時間採掘速度が上昇する",
        "",
        "LRL: Tree Assist (50Lv)",
        "木を破壊した際に繋がっているも一括で破壊する"
    )),
    HERBALISM(Material.STONE_HOE, "Herbalism", listOf()),
    BREEDING(Material.MILK_BUCKET, "Breeding", listOf()),
    BLACKSMITH(Material.SMITHING_TABLE, "BlackSmith", listOf()),
    REPAIR(Material.ANVIL, "Repair", listOf());

    fun getIcon(): Material = icon
    fun getExpBossBarKey(player: Player): NamespacedKey = NamespacedKey(instance, name + "_ExpBar_" + player.uniqueId.toString())

}
