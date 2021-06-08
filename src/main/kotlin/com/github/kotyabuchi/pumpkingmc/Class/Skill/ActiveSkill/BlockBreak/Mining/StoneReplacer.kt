package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak.Mining

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToggleSkillMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import java.util.*

object StoneReplacer: ToggleSkillMaster {
    override val skillName: String = "StoneReplacer"
    override val cost: Int = 0
    override val needLevel: Int = 50
    override var description: String = ""
    override val activePlayerLevelMap: MutableMap<UUID, Int> = mutableMapOf()

    private fun canReplace(player: Player): Boolean {
        if (!isEnabledSkill(player)) return false
        val inv = player.inventory
        val stoneSlotItem = inv.getItem(inv.heldItemSlot + 1) ?: return false
        return (stoneSlotItem.amount > 0 && (stoneSlotItem.type == Material.STONE || stoneSlotItem.type == Material.COBBLESTONE))
    }

    private fun replace(player: Player, block: Block) {
        if (!canReplace(player)) return
        val inv = player.inventory
        val stoneSlotItem = inv.getItem(inv.heldItemSlot + 1) ?: return
        block.type = stoneSlotItem.type
        stoneSlotItem.amount--
    }

    @EventHandler
    fun onBlockMine(event: BlockMineEvent) {
        if (event.isMultiBreak) return
        val player = event.player
        if (!isEnabledSkill(player)) return
        replace(player, event.block)
    }
}