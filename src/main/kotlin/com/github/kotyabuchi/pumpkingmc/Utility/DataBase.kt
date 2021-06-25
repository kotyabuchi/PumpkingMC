package com.github.kotyabuchi.pumpkingmc.Utility

import com.github.kotyabuchi.pumpkingmc.Enum.JobClassType
import com.github.kotyabuchi.pumpkingmc.System.Player.*
import com.github.kotyabuchi.pumpkingmc.instance
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement

fun initDB() {
    val dbFile = File(instance.dataFolder, "PumpkingFantasy.db")
    val dbHeader = "jdbc:sqlite:" + dbFile.absolutePath
    var stmt: Statement
    var pstmt: PreparedStatement
    try {
        Class.forName("org.sqlite.JDBC")
        try {
            DriverManager.getConnection(dbHeader).use { conn ->  //try-with-resources
                conn.autoCommit = false
                stmt = conn.createStatement()
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_skill_status (uuid TEXT NOT NULL, skill_id INTEGER NOT NULL, skill_total_exp REAL NOT NULL, UNIQUE(uuid, skill_id))")
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS skills (skill_id INTEGER PRIMARY KEY AUTOINCREMENT, skill_name TEXT UNIQUE NOT NULL)")
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS homes (home_id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL, home_name TEXT NOT NULL, world TEXT NOT NULL, x REAL NOT NULL, y REAL NOT NULL, z REAL NOT NULL, yaw REAL NOT NULL, icon TEXT)")
                conn.commit()

                try {
                    pstmt = conn.prepareStatement("INSERT INTO skills(skill_name) SELECT ? WHERE NOT EXISTS(SELECT 1 FROM skills WHERE skill_name = ?)")
                    JobClassType.values().forEach { skill ->
                        pstmt.setString(1, skill.name)
                        pstmt.setString(2, skill.name)
                        pstmt.addBatch()
                    }
                    pstmt.executeBatch()
                    conn.commit()
                } catch (e: SQLException) {
                    conn.rollback()
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }
}

fun loadPlayerStatus(vararg players: Player): List<PlayerStatus> {
//    initDB()
    val dbFile = File(instance.dataFolder, "PumpkingFantasy.db")
    val dbHeader = "jdbc:sqlite:" + dbFile.absolutePath
    var pstmt: PreparedStatement

    val result = mutableListOf<PlayerStatus>()
    if (players.isNotEmpty()) {
        try {
            DriverManager.getConnection(dbHeader).use { conn ->  //try-with-resources
                players.forEach { player ->
                    val playerStatus = PlayerStatus(player)
                    pstmt = conn.prepareStatement("SELECT * FROM player_skill_status INNER JOIN skills ON (player_skill_status.skill_id = skills.skill_id) WHERE player_skill_status.uuid = ?")
                    pstmt.setString(1, player.uniqueId.toString())
                    val skillRs = pstmt.executeQuery()

                    while (skillRs.next()) {
                        val jobClassStatus = JobClassStatus()
                        jobClassStatus.setTotalExp(skillRs.getDouble("skill_total_exp"))
                        playerStatus.setJobClassStatus(JobClassType.valueOf(skillRs.getString("skill_name")).jobClass, jobClassStatus)
                    }

                    pstmt = conn.prepareStatement("SELECT * FROM homes WHERE uuid = ?")
                    pstmt.setString(1, player.uniqueId.toString())
                    val homeRs = pstmt.executeQuery()

                    while (homeRs.next()) {
                        val homeId = homeRs.getInt("home_id")
                        val homeName = homeRs.getString("home_name")
                        val world = instance.server.getWorld(homeRs.getString("world"))
                        val x = homeRs.getDouble("x")
                        val y = homeRs.getDouble("y")
                        val z = homeRs.getDouble("z")
                        val yaw = homeRs.getFloat("yaw")
                        val icon = Material.valueOf(homeRs.getString("icon") ?: "ENDER_PEARL")
                        world?.let {
                            playerStatus.homes.add(Home(homeId, homeName, world, x, y, z, yaw, icon))
                        }
                    }
                    result.add(playerStatus)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    return result
}

@Synchronized
fun savePlayerStatus(vararg statusList: PlayerStatus = getAllPlayerStatus().toTypedArray()) {
    println("&a[System]PlayerStatusをデータベースに保存開始...".colorS())
    var success = true
//    initDB()
    val dbFile = File(instance.dataFolder, "PumpkingFantasy.db")
    var stmt: Statement
    val dbHeader = "jdbc:sqlite:" + dbFile.absolutePath
    var pstmt: PreparedStatement
    if (statusList.isNotEmpty()) {
        try {
            DriverManager.getConnection(dbHeader).use { conn ->  //try-with-resources
                conn.autoCommit = false
                stmt = conn.createStatement()
                val rs = stmt.executeQuery("SELECT * FROM skills")
                conn.commit()
                val skills = mutableMapOf<JobClassType, Int>()
                while (rs.next()) {
                    skills[JobClassType.valueOf(rs.getString("skill_name"))] = rs.getInt("skill_id")
                }
                try {
                    pstmt = conn.prepareStatement("REPLACE INTO player_skill_status VALUES (?, ?, ?)")
                    statusList.forEach { status ->
                        JobClassType.values().forEach { skill ->
                            skills[skill]?.let { skillId ->
                                val jobClassStatus = status.getJobClassStatus(skill.jobClass)
                                pstmt.setString(1, status.player.uniqueId.toString())
                                pstmt.setInt(2, skillId)
                                pstmt.setDouble(3, jobClassStatus.getTotalExp())
                                pstmt.addBatch()
                            }
                        }
                        println("&a[System]${status.player.name}'s status saved'".colorS())
                    }
                    pstmt.executeBatch()
                    conn.commit()
                } catch (e: SQLException) {
                    conn.rollback()
                    e.printStackTrace()
                    success = false
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            success = false
        }
    }
    if (success) {
        println("&a[System]PlayerStatusをデータベースに保存しました".colorS())
    } else {
        println("&4[System]PlayerStatusの保存に失敗しました".colorS())
    }
}

@Synchronized
fun removeHome(homeId: Int) {
//    initDB()
    val dbFile = File(instance.dataFolder, "PumpkingFantasy.db")
    val dbHeader = "jdbc:sqlite:" + dbFile.absolutePath
    var pstmt: PreparedStatement
    try {
        DriverManager.getConnection(dbHeader).use { conn ->  //try-with-resources
            pstmt = conn.prepareStatement("DELETE FROM homes WHERE home_id = ?")
            pstmt.setInt(1, homeId)
            pstmt.execute()
        }
    } catch (e: SQLException) {
        e.printStackTrace()
    }
}

fun addHome(player: Player, homeName: String, location: Location): Int? {
//    initDB()
    val dbFile = File(instance.dataFolder, "PumpkingFantasy.db")
    val dbHeader = "jdbc:sqlite:" + dbFile.absolutePath
    var pstmt: PreparedStatement

    val block = location.block.location.add(.5, .0, .5)
    val world = location.world ?: return null
    val worldName = world.name
    val x = block.x
    val y = location.y
    val z = block.z
    val yaw = location.yaw
    try {
        DriverManager.getConnection(dbHeader).use { conn ->  //try-with-resources
            pstmt = conn.prepareStatement("INSERT INTO homes(uuid, home_name, world, x, y, z, yaw, icon) VALUES (?,?,?,?,?,?,?,?)")
            pstmt.setString(1, player.uniqueId.toString())
            pstmt.setString(2, homeName)
            pstmt.setString(3, worldName)
            pstmt.setDouble(4, x)
            pstmt.setDouble(5, y)
            pstmt.setDouble(6, z)
            pstmt.setFloat(7, yaw)
            pstmt.setString(8, "ENDER_PEARL")
            pstmt.executeUpdate()

            val rs = conn.createStatement().executeQuery("SELECT LAST_INSERT_ROWID()")
            if (rs.next()) {
                val homeId = rs.getInt("LAST_INSERT_ROWID()")
                player.getStatus().homes.add(Home(homeId, homeName, world, x, y, z, yaw, Material.ENDER_PEARL))
                return homeId
            }
        }
    } catch (e: SQLException) {
        e.printStackTrace()
    }
    return null
}

fun changeHomeIcon(player: Player, home: Home, icon: Material) {
//    initDB()
    val dbFile = File(instance.dataFolder, "PumpkingFantasy.db")
    val dbHeader = "jdbc:sqlite:" + dbFile.absolutePath
    var pstmt: PreparedStatement

    home.homeId?.let { homeId ->
        try {
            DriverManager.getConnection(dbHeader).use { conn ->  //try-with-resources
                pstmt = conn.prepareStatement("REPLACE INTO homes VALUES (?,?,?,?,?,?,?,?,?)")
                pstmt.setInt(1, homeId)
                pstmt.setString(2, player.uniqueId.toString())
                pstmt.setString(3, home.name)
                pstmt.setString(4, home.world.name)
                pstmt.setDouble(5, home.x)
                pstmt.setDouble(6, home.y)
                pstmt.setDouble(7, home.z)
                pstmt.setFloat(8, home.yaw)
                pstmt.setString(9, icon.name)
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}

fun startAutoSave() {
    val time: Long = 20 * 60 * 30
    object : BukkitRunnable() {
        override fun run() {
            savePlayerStatus()
        }
    }.runTaskTimer(instance, time, time)
}