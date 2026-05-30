package com.blockvillage.app.world

import com.blockvillage.app.renderer.Ray
import kotlin.math.abs
import kotlin.math.floor

object Raycaster {

    data class HitResult(
        val blockX: Int, val blockY: Int, val blockZ: Int,
        val faceX: Int, val faceY: Int, val faceZ: Int,
        val distance: Float
    )

    fun cast(world: World, ray: Ray, maxDist: Float = 30f): HitResult? {
        var x = floor(ray.ox).toInt()
        var y = floor(ray.oy).toInt()
        var z = floor(ray.oz).toInt()

        val stepX = if (ray.dx > 0) 1 else -1
        val stepY = if (ray.dy > 0) 1 else -1
        val stepZ = if (ray.dz > 0) 1 else -1

        val tDeltaX = if (ray.dx == 0f) Float.MAX_VALUE else abs(1f / ray.dx)
        val tDeltaY = if (ray.dy == 0f) Float.MAX_VALUE else abs(1f / ray.dy)
        val tDeltaZ = if (ray.dz == 0f) Float.MAX_VALUE else abs(1f / ray.dz)

        var tMaxX = if (ray.dx > 0) (x+1-ray.ox)*tDeltaX else (ray.ox-x)*tDeltaX
        var tMaxY = if (ray.dy > 0) (y+1-ray.oy)*tDeltaY else (ray.oy-y)*tDeltaY
        var tMaxZ = if (ray.dz > 0) (z+1-ray.oz)*tDeltaZ else (ray.oz-z)*tDeltaZ

        var lx = x; var ly = y; var lz = z
        var t = 0f

        while (t < maxDist) {
            val block = world.getBlock(x, y, z)
            if (block.isSolid && block != BlockType.WATER) {
                return HitResult(x, y, z, lx, ly, lz, t)
            }
            lx = x; ly = y; lz = z
            when {
                tMaxX < tMaxY && tMaxX < tMaxZ -> { x += stepX; t = tMaxX; tMaxX += tDeltaX }
                tMaxY < tMaxZ -> { y += stepY; t = tMaxY; tMaxY += tDeltaY }
                else -> { z += stepZ; t = tMaxZ; tMaxZ += tDeltaZ }
            }
        }
        return null
    }
}
