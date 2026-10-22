package com.example.digitidentifier

import android.content.Context
import java.io.File

object ResultLogger {
    // Change this label whenever you change the preprocessing
    const val PREPROCESS_VERSION = "v3-aug+bbox"

    private fun file(context: Context) = File(context.filesDir, "results.csv")

    fun log(context: Context, predicted: Int, actual: Int, confidence: Float) {
        val f = file(context)
        if (!f.exists()) f.writeText("timestamp,version,predicted,actual,confidence,correct\n")
        val correct = predicted == actual
        f.appendText(
            "${System.currentTimeMillis()},$PREPROCESS_VERSION,$predicted,$actual," +
                    "${"%.4f".format(confidence)},$correct\n"
        )
    }

    // One line per preprocessing version, e.g. "v2-bbox: 41/50 correct (82%)"
    fun summary(context: Context): String {
        val f = file(context)
        if (!f.exists()) return "No results yet"
        val rows = f.readLines().drop(1).filter { it.isNotBlank() }
        return rows.groupBy { it.split(",")[1] }.entries.joinToString("\n") { (version, rs) ->
            val correct = rs.count { it.endsWith("true") }
            "$version: $correct/${rs.size} correct (${correct * 100 / rs.size}%)"
        }
    }
}