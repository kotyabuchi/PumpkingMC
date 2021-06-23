package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.Enum.Symbol
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import kotlin.math.floor

object ChatUtil {
    fun separator(title: String = "", color: NamedTextColor = NamedTextColor.WHITE, footer: Boolean = false): Component {
        var result = "========="
        if (footer) {
            repeat(title.length + 2) {
                result += "="
            }
            result += "========="
        } else {
            result += " $title ========="
        }
        return Component.text(result, color, TextDecoration.BOLD).normal()
    }
}

fun String.colorS(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}

fun String.upperCamelCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase()
        else -> {
            var result = ""
            this.split("_").forEach {
                result += it[0].toUpperCase() + it.substring(1).toLowerCase() + " "
            }
            result.trim()
        }
    }
}

fun Double.floor1Digits(): Double {
    return floor(this * 10.0) / 10.0
}

fun Double.floor2Digits(): Double {
    return floor(this * 100.0) / 100.0
}

fun Double.floor3Digits(): Double {
    return floor(this * 1000.0) / 1000.0
}

fun Int.toRomanNumeral(): String = Symbol.closestBelow(this)
        .let { symbol ->
            if (symbol != null) {
                "$symbol${(this - symbol.decimalValue).toRomanNumeral()}"
            } else {
                ""
            }
        }

fun String.toDecimal() : Int {
    return Symbol.highestStartingSymbol(this)
            .let{ symbol ->
                if (symbol != null) {
                    symbol.decimalValue + this.drop(symbol.name.length).toDecimal()
                } else {
                    0
                }
            }
}

fun Enchantment.toLore(level: Int): String {
    var result = this.name.upperCamelCase()
    if (this.maxLevel > 1) {
        result += " ${level.toRomanNumeral()}"
    }
    return result
}

fun TextComponent.normal(vararg decoration: TextDecoration): TextComponent {
    var result = this
    if (decoration.isEmpty()) {
        result = this.decoration(TextDecoration.ITALIC, false)
    } else {
        decoration.forEach {
            result = this.decoration(it, false)
        }
    }
    return result
}
