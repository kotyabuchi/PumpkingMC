package com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.BlockBreak

import com.github.kotyabuchi.pumpkingmc.Class.JobClassMaster
import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToggleSkillMaster
import com.github.kotyabuchi.pumpkingmc.CustomEvent.BlockMineEvent
import com.github.kotyabuchi.pumpkingmc.System.ItemExpansion
import com.github.kotyabuchi.pumpkingmc.Utility.consume
import com.github.kotyabuchi.pumpkingmc.Utility.reverse
import com.github.kotyabuchi.pumpkingmc.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class GroundLevelingAssist(private val jobClass: JobClassMaster): ToggleSkillMaster {
    override val skillName: String = "GroundLevelingAssist"
    override val cost: Int = 0
    override val needLevel: Int = 200
    override var description: String = "破壊したBlockの裏にBlockがなかった場合、手持ちのBlockを設置する"

    private val groundLevelingAssisBlockSet = mutableSetOf<Material>()

    init {
        instance.registerEvent(this)
    }

    override fun getSkillNamespacedKey(): NamespacedKey = NamespacedKey(instance, "${skillName}_${jobClass.jobClassName}")

    override fun enableAction(player: Player, level: Int) {
        player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        player.sendActionBar(Component.text("$skillName On", NamedTextColor.GREEN))
    }

    override fun disableAction(player: Player) {
        player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f)
        player.sendActionBar(Component.text("$skillName Off", NamedTextColor.RED))
    }

    fun addAssistBlock(vararg materials: Material) {
        groundLevelingAssisBlockSet.addAll(materials)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event is BlockMineEvent) return
        if (event.isCancelled) return
        val player = event.player
        val block = event.block
        val itemStack = player.inventory.itemInMainHand

        if (!jobClass.getTool().contains(itemStack.type)) return

        if (isEnabledSkill(player)) {
            val rayBlock = player.rayTraceBlocks(8.0)
            rayBlock?.hitBlockFace?.reverse()?.let { lookingFace ->
                val targetBlock = block.getRelative(lookingFace)
                if (targetBlock.type.isAir) {
                    for (groundLevelingMaterial in groundLevelingAssisBlockSet) {
                        if (player.inventory.consume(ItemStack(groundLevelingMaterial))) {
                            targetBlock.type = groundLevelingMaterial
                            break
                        }
                    }
                }
            }
        }
    }
}