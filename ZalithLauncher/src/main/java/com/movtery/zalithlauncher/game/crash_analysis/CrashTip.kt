package com.movtery.zalithlauncher.game.crash_analysis

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The output contains only fixed explanations and, when explicitly reported, a safe mod ID. */
@Parcelize
data class CrashTip(
    val severity: Severity,
    val title: String,
    val description: String,
    val solution: String
) : Parcelable

enum class Severity { INFO, WARNING, ERROR }

data class CrashContext(
    val exitCode: Int,
    val isSignal: Boolean,
    val allocatedRamMb: Int = 0,
    val renderer: String = "",
    val javaVersion: String = "",
    val gameHome: String = "",
    val logContent: String = "",
    val mods: List<ModInfo> = emptyList(),
    val reportContent: String = ""
)

// Retained for the existing standalone mod scanner; the analyzer does not scan mod files.
data class ModInfo(val id: String, val name: String, val version: String)
