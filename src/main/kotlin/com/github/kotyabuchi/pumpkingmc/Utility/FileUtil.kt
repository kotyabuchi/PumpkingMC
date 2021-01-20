package com.github.kotyabuchi.pumpkingmc.Utility

import java.io.*
import java.util.*

fun saveFile(path: String, vararg objs: Any) {
    try {
        val file = File(path)

        if (!file.exists()) file.createNewFile()
        val fw = FileWriter(file)
        val pw = PrintWriter(BufferedWriter(fw))
        for (obj in objs) {
            pw.print(obj)
        }
        pw.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun readFile(file: File): String {
    var result = ""
    if (file.exists()) {
        val scan = Scanner(file)
        var i = scan.nextLine()
        if (i != null) {
            result = i
        }
    }
    return result
}
