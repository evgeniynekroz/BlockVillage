package com.blockvillage.app.renderer

import android.opengl.Matrix
import kotlin.math.*

class Camera {
    var distance = 40f
    var azimuth = 45f
    var elevation = 30f
    var pivotX = 32f
    var pivotY = 4f
    var pivotZ = 32f

    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    val vpMatrix = FloatArray(16)
    private var aspectRatio = 1f

    fun setAspectRatio(aspect: Float) {
        aspectRatio = aspect
        updateProjection()
        updateView()
    }

    fun updateProjection() {
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 0.1f, 500f)
    }

    fun updateView() {
        val azRad = Math.toRadians(azimuth.toDouble()).toFloat()
        val elRad = Math.toRadians(elevation.toDouble()).toFloat()
        val eyeX = pivotX + distance * cos(elRad) * sin(azRad)
        val eyeY = pivotY + distance * sin(elRad)
        val eyeZ = pivotZ + distance * cos(elRad) * cos(azRad)
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, pivotX, pivotY, pivotZ, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    fun rotate(deltaAzimuth: Float, deltaElevation: Float) {
        azimuth += deltaAzimuth
        elevation = (elevation + deltaElevation).coerceIn(5f, 85f)
        updateView()
    }

    fun zoom(delta: Float) {
        distance = (distance * delta).coerceIn(8f, 100f)
        updateView()
    }

    fun pan(deltaX: Float, deltaY: Float) {
        val azRad = Math.toRadians(azimuth.toDouble()).toFloat()
        val elRad = Math.toRadians(elevation.toDouble()).toFloat()
        val forwardX = -sin(azRad)
        val forwardZ = -cos(azRad)
        val rightX = cos(azRad)
        val rightZ = -sin(azRad)
        val speed = distance * 0.005f
        pivotX += (rightX * deltaX + forwardX * deltaY) * speed
        pivotZ += (rightZ * deltaX + forwardZ * deltaY) * speed
        updateView()
    }

    fun screenToRay(screenX: Float, screenY: Float, viewWidth: Float, viewHeight: Float): Ray {
        val invVP = FloatArray(16)
        Matrix.invertM(invVP, 0, vpMatrix, 0)
        val ndcX = (2f * screenX) / viewWidth - 1f
        val ndcY = 1f - (2f * screenY) / viewHeight
        val near = floatArrayOf(ndcX, ndcY, -1f, 1f)
        val far = floatArrayOf(ndcX, ndcY, 1f, 1f)
        val nw = FloatArray(4); val fw = FloatArray(4)
        Matrix.multiplyMV(nw, 0, invVP, 0, near, 0)
        Matrix.multiplyMV(fw, 0, invVP, 0, far, 0)
        val ox = nw[0]/nw[3]; val oy = nw[1]/nw[3]; val oz = nw[2]/nw[3]
        val fx = fw[0]/fw[3]; val fy = fw[1]/fw[3]; val fz = fw[2]/fw[3]
        val dx = fx - ox; val dy = fy - oy; val dz = fz - oz
        val len = sqrt(dx*dx + dy*dy + dz*dz)
        return Ray(ox, oy, oz, dx/len, dy/len, dz/len)
    }
}

data class Ray(
    val ox: Float, val oy: Float, val oz: Float,
    val dx: Float, val dy: Float, val dz: Float
)
