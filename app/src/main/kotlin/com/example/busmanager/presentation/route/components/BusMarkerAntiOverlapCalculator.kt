package com.example.busmanager.presentation.route.components

import com.example.busmanager.domain.route.model.MapCoordinate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class DisplayMarkerOffset(
    val dxPx: Float = 0f,
    val dyPx: Float = 0f
)

object BusMarkerAntiOverlapCalculator {
    const val DEFAULT_COLLISION_RADIUS_PX = 32f
    const val SPREAD_RADIUS_PX = 18f

    fun <T> calculateOffsets(
        items: List<T>,
        coordinateSelector: (T) -> MapCoordinate,
        canvasWidth: Float,
        canvasHeight: Float,
        collisionRadiusPx: Float = DEFAULT_COLLISION_RADIUS_PX,
        spreadRadiusPx: Float = SPREAD_RADIUS_PX
    ): Map<T, DisplayMarkerOffset> {
        if (items.isEmpty()) return emptyMap()
        if (items.size == 1) return mapOf(items.first() to DisplayMarkerOffset())
        if (canvasWidth <= 0f || canvasHeight <= 0f) return items.associateWith { DisplayMarkerOffset() }

        val pixelPositions = items.map { item ->
            val coord = coordinateSelector(item)
            val px = coord.x * canvasWidth
            val py = coord.y * canvasHeight
            Triple(item, px, py)
        }

        val visited = mutableSetOf<Int>()
        val clusters = mutableListOf<List<Int>>()

        for (i in pixelPositions.indices) {
            if (i in visited) continue
            val cluster = mutableListOf<Int>()
            val queue = ArrayDeque<Int>()
            queue.add(i)
            visited.add(i)

            while (queue.isNotEmpty()) {
                val curr = queue.removeFirst()
                cluster.add(curr)
                val (_, cx, cy) = pixelPositions[curr]

                for (j in pixelPositions.indices) {
                    if (j !in visited) {
                        val (_, ox, oy) = pixelPositions[j]
                        val distSq = (cx - ox) * (cx - ox) + (cy - oy) * (cy - oy)
                        if (distSq <= collisionRadiusPx * collisionRadiusPx) {
                            visited.add(j)
                            queue.add(j)
                        }
                    }
                }
            }
            clusters.add(cluster)
        }

        val resultMap = mutableMapOf<T, DisplayMarkerOffset>()

        for (cluster in clusters) {
            when (cluster.size) {
                1 -> {
                    val item = pixelPositions[cluster[0]].first
                    resultMap[item] = DisplayMarkerOffset(0f, 0f)
                }
                2 -> {
                    val item1 = pixelPositions[cluster[0]].first
                    val item2 = pixelPositions[cluster[1]].first
                    resultMap[item1] = DisplayMarkerOffset(-spreadRadiusPx, 0f)
                    resultMap[item2] = DisplayMarkerOffset(spreadRadiusPx, 0f)
                }
                else -> {
                    val n = cluster.size
                    val stepAngle = (2.0 * PI) / n
                    for ((k, idx) in cluster.withIndex()) {
                        val item = pixelPositions[idx].first
                        val angle = (k * stepAngle) - (PI / 2.0)
                        val dx = (spreadRadiusPx * cos(angle)).toFloat()
                        val dy = (spreadRadiusPx * sin(angle)).toFloat()
                        resultMap[item] = DisplayMarkerOffset(dx, dy)
                    }
                }
            }
        }

        return resultMap
    }
}
