package com.movtery.zalithlauncher.ui.screens.game.elements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameBallGeometryTest {
    private val bounds = GameBallBounds(20f, 10f, 800f, 400f)

    @Test fun clampAndDockRespectPhysicalEdges() {
        assertEquals(GameBallPoint(20f, 400f), bounds.clamp(GameBallPoint(-5f, 900f)))
        assertEquals(GameBallPoint(20f, 100f), bounds.dock(GameBallPoint(500f, 100f), GameBallDock.LEFT))
        assertEquals(GameBallPoint(800f, 100f), bounds.dock(GameBallPoint(100f, 100f), GameBallDock.RIGHT))
        assertEquals(GameBallPoint(20f, 100f), bounds.dock(GameBallPoint(200f, 100f), GameBallDock.NEAREST))
        assertEquals(GameBallPoint(800f, 100f), bounds.dock(GameBallPoint(700f, 100f), GameBallDock.NEAREST))
        assertEquals(GameBallPoint(500f, 100f), bounds.dock(GameBallPoint(500f, 100f), GameBallDock.FREE))
    }

    @Test fun normalizedPositionSurvivesRotationAndContentResize() {
        val original = GameBallPoint(215f, 205f)
        val fraction = bounds.fraction(original)
        val rotated = GameBallBounds(0f, 40f, 300f, 800f)
        val restored = rotated.fromFraction(fraction)
        assertEquals(original.x, bounds.fromFraction(fraction).x, 0.001f)
        assertEquals(0.25f, rotated.fraction(restored).x, 0.001f)
        assertEquals(0.5f, rotated.fraction(restored).y, 0.001f)
        assertEquals(rotated.right, rotated.dock(restored, GameBallDock.RIGHT).x, 0.001f)
    }

    @Test fun windowInsetsAndContainerOffsetAreIntersected() {
        val b = gameBallBounds(
            parentWidth = 900f, parentHeight = 500f, parentX = 50f, parentY = 20f,
            windowWidth = 1000f, windowHeight = 600f,
            insetLeft = 80f, insetTop = 40f, insetRight = 100f, insetBottom = 100f,
            ballWidth = 120f, ballHeight = 60f
        )
        assertEquals(GameBallBounds(30f, 20f, 730f, 420f), b)
    }

    @Test fun alreadyInsetContainerDoesNotAddInsetsAgain() {
        val b = gameBallBounds(800f, 400f, 100f, 50f, 1000f, 500f, 80f, 40f, 80f, 40f, 50f, 50f)
        assertEquals(GameBallBounds(0f, 0f, 750f, 350f), b)
    }

    @Test fun allConsumingInsetsDoNotHideTheButton() {
        val b = gameBallBounds(50f, 40f, 0f, 0f, 50f, 40f, 40f, 30f, 40f, 30f, 20f, 20f)
        assertEquals(GameBallBounds(0f, 0f, 30f, 20f), b)
    }

    @Test fun oversizedButtonAndInvalidPositionNeverProduceInvalidRanges() {
        val b = gameBallBounds(100f, 50f, 0f, 0f, 100f, 50f, 10f, 10f, 10f, 10f, 500f, 500f)
        assertEquals(b.left, b.right, 0f)
        assertEquals(b.top, b.bottom, 0f)
        val p = b.clamp(GameBallPoint(Float.NaN, Float.POSITIVE_INFINITY))
        assertTrue(p.x.isFinite() && p.y.isFinite())
        assertEquals(GameBallPoint(b.left, b.top), p)
    }
}
