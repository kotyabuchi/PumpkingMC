package com.github.kotyabuchi.pumpkingmc.Menu.MenuItem

import com.github.kotyabuchi.pumpkingmc.Class.Skill.ActiveSkill.ToggleSkillMaster
import com.github.kotyabuchi.pumpkingmc.Enum.SkillCommand
import com.github.kotyabuchi.pumpkingmc.Utility.ItemStackGenerator
import com.github.kotyabuchi.pumpkingmc.Utility.colorS
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SkillInfoButton(skill: ToggleSkillMaster, command: SkillCommand): MenuButtonBase() {

    init {
        clickSound = null
        val lore = mutableListOf<String>()
        lore.add("&f&lCommand&r&f > ${command.name.replace("L", "&aL").replace("R", "&cR").colorS()}")
        lore.add("&f&lNeedLevel&r&f > ${skill.needLevel}")
        lore.add("")
        lore.add("&f${skill.description}")
        menuItem = ItemStackGenerator(ItemStack(Material.GREEN_DYE)).setDisplayName(skill.skillName).setLore(lore).setMenuItemTag().generate()
    }
}