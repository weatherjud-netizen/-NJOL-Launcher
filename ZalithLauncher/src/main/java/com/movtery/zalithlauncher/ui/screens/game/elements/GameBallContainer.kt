/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import kotlin.math.roundToInt

/** The overlay is an ordinary Compose child of the game window; no overlay permission is used. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GameBallContainer(
    legacyPosition: Offset,
    onPositionChanged: (Offset) -> Unit,
    onSavePos: () -> Unit,
    onClick: () -> Unit,
    alpha: Float,
    content: @Composable (handle: Modifier, size: Dp) -> Unit
) {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val view = LocalView.current
    // Reserve the hidden navigation bars too, so the button remains reachable when they appear.
    val insets = WindowInsets.displayCutout
        .union(WindowInsets.systemBarsIgnoringVisibility)
        .union(WindowInsets.mandatorySystemGestures)
    val insetLeft = insets.getLeft(density, direction).toFloat()
    val insetRight = insets.getRight(density, direction).toFloat()
    val insetTop = insets.getTop(density).toFloat()
    val insetBottom = insets.getBottom(density).toFloat()
    val dock = GameBallPreferences.dockMode
    var windowOrigin by remember { mutableStateOf(Offset.Zero) }
    var windowSize by remember { mutableStateOf(IntSize.Zero) }
    var ballSize by remember { mutableStateOf(IntSize.Zero) }
    var relative by remember { mutableStateOf(GameBallPreferences.loadPosition()) }
    var dragPosition by remember { mutableStateOf<GameBallPoint?>(null) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val currentOnSavePos by rememberUpdatedState(onSavePos)
    val description = stringResource(R.string.game_ball_description)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
            windowOrigin = coordinates.positionInWindow()
            windowSize = IntSize(view.rootView.width, view.rootView.height)
        }
    ) {
        val parentWidth = constraints.maxWidth.toFloat()
        val parentHeight = constraints.maxHeight.toFloat()
        val ready = windowSize.width > 0 && windowSize.height > 0
        val region = gameBallBounds(
            parentWidth, parentHeight, windowOrigin.x, windowOrigin.y,
            windowSize.width.toFloat(), windowSize.height.toFloat(),
            insetLeft, insetTop, insetRight, insetBottom, 0f, 0f
        )
        val bounds = gameBallBounds(
            parentWidth, parentHeight, windowOrigin.x, windowOrigin.y,
            windowSize.width.toFloat(), windowSize.height.toFloat(),
            insetLeft, insetTop, insetRight, insetBottom,
            ballSize.width.toFloat(), ballSize.height.toFloat()
        )
        val currentBounds by rememberUpdatedState(bounds)
        val currentDock by rememberUpdatedState(dock)
        val currentRelative by rememberUpdatedState(relative)
        val currentLegacyPosition by rememberUpdatedState(legacyPosition)
        val positioned = ballSize.width > 0 && ballSize.height > 0
        val displayed = dragPosition?.let { bounds.clamp(it) }
            ?: bounds.fromFraction(relative ?: GameBallPoint(0.5f, 0f))
        val currentDisplayed by rememberUpdatedState(displayed)

        // Migrate the old absolute-pixel position once. Thereafter the normalized position
        // survives rotation, resizing, density changes and the ball's changing content width.
        LaunchedEffect(ready, positioned, bounds, dock, relative, dragPosition != null) {
            if (!ready || !positioned || dragPosition != null) return@LaunchedEffect
            var fraction = relative
            if (fraction == null) {
                val old = currentLegacyPosition
                fraction = if (old == Offset.Zero || !old.x.isFinite() || !old.y.isFinite()) {
                    GameBallPoint(0.5f, 0f)
                } else {
                    bounds.fraction(GameBallPoint(old.x, old.y))
                }
                relative = fraction
                GameBallPreferences.savePosition(fraction)
            }
            if (dock != GameBallDock.FREE && bounds.right > bounds.left) {
                val target = bounds.dock(bounds.fromFraction(fraction), dock)
                val snapped = fraction.copy(x = bounds.fraction(target).x)
                if (snapped != fraction) {
                    relative = snapped
                    GameBallPreferences.savePosition(snapped)
                    currentOnPositionChanged(Offset(target.x, target.y))
                    currentOnSavePos()
                }
            }
        }

        if (ready && region.right > region.left && region.bottom > region.top) {
            val maxWidth = with(density) { (region.right - region.left).toDp() }
            val maxHeight = with(density) { (region.bottom - region.top).toDp() }
            val handleSize = minOf(GameBallPreferences.size.state.dp, maxWidth, maxHeight)
            Surface(
                modifier = Modifier
                    .align(AbsoluteAlignment.TopLeft)
                    .absoluteOffset { IntOffset(displayed.x.roundToInt(), displayed.y.roundToInt()) }
                    .widthIn(max = maxWidth)
                    .heightIn(max = maxHeight)
                    .onSizeChanged { ballSize = it }
                    .alpha(alpha),
                color = Color.Black.copy(alpha = 0.25f),
                contentColor = Color.White.copy(alpha = 0.95f),
                shape = MaterialTheme.shapes.medium
            ) {
                // Only the handle owns the drag/tap gesture; recorder buttons remain independent.
                val handle = Modifier
                    .focusProperties { canFocus = false }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragPosition = currentDisplayed },
                            onDragEnd = {
                                val b = currentBounds
                                val target = b.dock(dragPosition ?: currentDisplayed, currentDock)
                                val measured = b.fraction(target)
                                val previous = currentRelative ?: GameBallPoint(0.5f, 0f)
                                val saved = GameBallPoint(
                                    if (b.right > b.left) measured.x else previous.x,
                                    if (b.bottom > b.top) measured.y else previous.y
                                )
                                relative = saved
                                GameBallPreferences.savePosition(saved)
                                currentOnPositionChanged(Offset(target.x, target.y))
                                currentOnSavePos()
                                dragPosition = null
                            },
                            onDragCancel = { dragPosition = null }
                        ) { change, amount ->
                            val b = currentBounds
                            val p = dragPosition ?: currentDisplayed
                            dragPosition = b.clamp(GameBallPoint(p.x + amount.x, p.y + amount.y))
                            change.consume()
                        }
                    }
                    .semantics { contentDescription = description }
                    .clickable { currentOnClick() }
                content(handle, handleSize)
            }
        }
    }
}
