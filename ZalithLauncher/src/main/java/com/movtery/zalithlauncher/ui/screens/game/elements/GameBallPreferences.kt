/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.game.elements

import com.movtery.zalithlauncher.setting.launcherMMKV
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit

/** Kept separate from the shared launcher settings and initialized only in the game. */
internal object GameBallPreferences {
    val size = IntSettingUnit("gameBallSizeDp", 48, 48..80).also { it.init() }
    val dock = IntSettingUnit("gameBallDockMode", 0, 0..3).also { it.init() }
    val dockMode: GameBallDock get() = GameBallDock.entries[dock.state.coerceIn(0, 3)]

    fun loadPosition(): GameBallPoint? {
        val store = launcherMMKV()
        if (!store.getBoolean("gameBallRelativePositionSet", false)) return null
        val x = store.getFloat("gameBallRelativeX", 0.5f)
        val y = store.getFloat("gameBallRelativeY", 0f)
        if (!x.isFinite() || !y.isFinite()) return null
        return GameBallPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
    }

    fun savePosition(position: GameBallPoint) {
        val store = launcherMMKV()
        store.putFloat("gameBallRelativeX", position.x.coerceIn(0f, 1f))
            .putFloat("gameBallRelativeY", position.y.coerceIn(0f, 1f))
            .putBoolean("gameBallRelativePositionSet", true).apply()
    }
}
