package com.example.busmanager.presentation.route.components

data class MapWorldDimensions(
    val width: Float,
    val height: Float
)

object EastJavaMapProjection {
    const val ASPECT_RATIO = 2.0f
    const val MIN_ZOOM = 0.48f
    const val DEFAULT_ZOOM = 0.85f
    const val MAX_ZOOM = 4.0f

    fun calculateWorldDimensions(viewportWidth: Float, viewportHeight: Float): MapWorldDimensions {
        val safeHeight = viewportHeight.coerceAtLeast(320f)
        val safeWidth = (safeHeight * ASPECT_RATIO).coerceAtLeast(viewportWidth)
        return MapWorldDimensions(width = safeWidth, height = safeHeight)
    }

    fun calculateMaxPanX(worldWidth: Float, viewportWidth: Float, zoom: Float): Float {
        return (worldWidth * zoom - viewportWidth).coerceAtLeast(0f) / 2f
    }

    fun calculateMaxPanY(worldHeight: Float, viewportHeight: Float, zoom: Float): Float {
        return (worldHeight * zoom - viewportHeight).coerceAtLeast(0f) / 2f
    }

    fun calculateTargetPanX(coordX: Float, worldWidth: Float, zoom: Float, maxPanX: Float): Float {
        return ((0.5f - coordX) * worldWidth * zoom).coerceIn(-maxPanX, maxPanX)
    }

    fun calculateTargetPanY(coordY: Float, worldHeight: Float, zoom: Float, maxPanY: Float): Float {
        return ((0.5f - coordY) * worldHeight * zoom).coerceIn(-maxPanY, maxPanY)
    }
}
