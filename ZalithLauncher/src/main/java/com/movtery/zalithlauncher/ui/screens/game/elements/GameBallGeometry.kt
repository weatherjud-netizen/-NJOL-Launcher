/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.game.elements

/** Physical screen edges, independent of the application's text direction. */
internal enum class GameBallDock { FREE, LEFT, RIGHT, NEAREST }

internal data class GameBallPoint(val x: Float, val y: Float)

/** Available range of the top-left corner, after accounting for the whole ball. */
internal data class GameBallBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun clamp(point: GameBallPoint) = GameBallPoint(
        point.x.finiteOr(left).coerceIn(left, right),
        point.y.finiteOr(top).coerceIn(top, bottom)
    )

    fun fromFraction(point: GameBallPoint): GameBallPoint = GameBallPoint(
        left + (right - left) * point.x.finiteOr(0.5f).coerceIn(0f, 1f),
        top + (bottom - top) * point.y.finiteOr(0f).coerceIn(0f, 1f)
    )

    fun fraction(point: GameBallPoint): GameBallPoint {
        val p = clamp(point)
        return GameBallPoint(
            if (right > left) (p.x - left) / (right - left) else 0f,
            if (bottom > top) (p.y - top) / (bottom - top) else 0f
        )
    }

    fun dock(point: GameBallPoint, mode: GameBallDock): GameBallPoint {
        val p = clamp(point)
        return p.copy(x = when (mode) {
            GameBallDock.FREE -> p.x
            GameBallDock.LEFT -> left
            GameBallDock.RIGHT -> right
            GameBallDock.NEAREST -> if (p.x - left <= right - p.x) left else right
        })
    }
}

private fun Float.finiteOr(fallback: Float) = if (isFinite()) this else fallback

/** Window coordinates may start outside this particular Compose container. */
internal fun gameBallBounds(
    parentWidth: Float, parentHeight: Float,
    parentX: Float, parentY: Float,
    windowWidth: Float, windowHeight: Float,
    insetLeft: Float, insetTop: Float, insetRight: Float, insetBottom: Float,
    ballWidth: Float, ballHeight: Float
): GameBallBounds {
    val width = parentWidth.coerceAtLeast(0f)
    val height = parentHeight.coerceAtLeast(0f)
    var safeLeft = (insetLeft - parentX).coerceIn(0f, width)
    var safeTop = (insetTop - parentY).coerceIn(0f, height)
    var safeRight = (windowWidth - insetRight - parentX).coerceIn(safeLeft, width)
    var safeBottom = (windowHeight - insetBottom - parentY).coerceIn(safeTop, height)
    // If system insets consume an entire tiny multi-window, keeping the control
    // reachable takes precedence over an impossible fully inset placement.
    if (safeRight <= safeLeft && width > 0f) { safeLeft = 0f; safeRight = width }
    if (safeBottom <= safeTop && height > 0f) { safeTop = 0f; safeBottom = height }
    return GameBallBounds(
        safeLeft, safeTop,
        (safeRight - ballWidth.coerceAtLeast(0f)).coerceAtLeast(safeLeft),
        (safeBottom - ballHeight.coerceAtLeast(0f)).coerceAtLeast(safeTop)
    )
}
