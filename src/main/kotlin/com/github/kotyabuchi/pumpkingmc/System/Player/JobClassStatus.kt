package com.github.kotyabuchi.pumpkingmc.System.Player

import kotlin.math.round

class JobClassStatus {

    private var combo: Int = 0
    private var level: Int = 1
    private var exp: Double = 0.0
    private var totalExp: Double = 0.0
    private var recentAddedExp: Double = 0.0

    fun setLevel(point: Int) {
        level = point
        totalExp = 0.0
        for (i in 1 until level) {
            totalExp += getNextLevelExp(i)
        }
        totalExp += exp
    }

    fun setExp(point: Double) {
        exp = point
    }

    fun setTotalExp(point: Double) {
        var newLevel = 1
        var totalExp = point
        while (totalExp >= getNextLevelExp(newLevel)) {
            totalExp -= getNextLevelExp(newLevel)
            newLevel++
        }
        this.totalExp = point
        level = newLevel
        exp = totalExp
    }

    fun getLevel(): Int = level
    fun getExp(): Double = exp
    fun getTotalExp(): Double = totalExp
    fun getNextLevelExp(targetLevel: Int = level): Int = round((targetLevel * targetLevel / 1.5 + 10.0 * targetLevel) * (1 + targetLevel / 1000.0)).toInt()
    fun getCombo(): Int = combo
    fun getRecentAddedExp(): Double = recentAddedExp

    fun addExp(point: Double): AddExpResult {
        addCombo(1)
        val expMultiple = 1 + combo * 0.002
        val addExp = point * expMultiple
        recentAddedExp = addExp
        exp += addExp
        totalExp += addExp
        var result = AddExpResult.KEEP_LEVEL
        while (exp >= getNextLevelExp()) {
            exp -= getNextLevelExp()
            addLevel(1)
            result = AddExpResult.LEVEL_UP
        }
        return result
    }

    fun addLevel(point: Int) {
        level += point
    }

    fun addCombo(point: Int) {
        combo += point
    }

    fun resetCombo() {
        combo = 0
    }

    fun reset() {
        combo = 0
        level = 1
        exp = .0
        totalExp = .0
        recentAddedExp = .0
    }

    enum class AddExpResult {
        KEEP_LEVEL,
        LEVEL_UP
    }
}