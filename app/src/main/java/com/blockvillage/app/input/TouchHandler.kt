package com.blockvillage.app.input

import android.view.MotionEvent
import android.view.View
import com.blockvillage.app.renderer.BlockVillageRenderer
import com.blockvillage.app.world.BlockType
import com.blockvillage.app.world.Raycaster
import com.blockvillage.app.world.World
import kotlin.math.sqrt

class TouchHandler(
    private val renderer: BlockVillageRenderer,
    private val world: World,
    private val onBlockChanged: () -> Unit
) : View.OnTouchListener {

    private var mode = 0  // 0=none,1=rotate,2=pan,3=tap
    private var prevX = 0f; private var prevY = 0f
    private var startX = 0f; private var startY = 0f
    private var startTime = 0L
    private var pid1 = -1; private var pid2 = -1
    private var initPinchDist = 0f; private var initCamDist = 0f
    private var wasPinching = false
    private var spx = 0f; private var spy = 0f

    var selectedBlockType: BlockType = BlockType.PLANKS

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mode = 3; startX = event.x; startY = event.y
                prevX = event.x; prevY = event.y
                startTime = System.currentTimeMillis()
                pid1 = event.getPointerId(0); wasPinching = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    wasPinching = true; pid2 = event.getPointerId(1)
                    val i1 = event.findPointerIndex(pid1); val i2 = event.findPointerIndex(pid2)
                    if (i1 >= 0 && i2 >= 0) {
                        initPinchDist = dist(event.getX(i1), event.getY(i1), event.getX(i2), event.getY(i2))
                        initCamDist = renderer.camera.distance
                        spx = event.getX(i2); spy = event.getY(i2)
                    }
                    mode = 2
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (wasPinching && event.pointerCount == 2) {
                    val i1 = event.findPointerIndex(pid1); val i2 = event.findPointerIndex(pid2)
                    if (i1 >= 0 && i2 >= 0) {
                        val cd = dist(event.getX(i1), event.getY(i1), event.getX(i2), event.getY(i2))
                        if (initPinchDist > 0) {
                            renderer.camera.distance = (initCamDist * (initPinchDist / cd)).coerceIn(8f, 100f)
                            renderer.camera.updateView()
                        }
                        val cx = (event.getX(i1) + event.getX(i2)) / 2f
                        val cy = (event.getY(i1) + event.getY(i2)) / 2f
                        renderer.onTouchPan(cx - prevX, cy - prevY)
                        prevX = cx; prevY = cy
                    }
                } else if (mode == 1 || (mode == 3 && distFromStart(event) > 10f)) {
                    mode = 1
                    renderer.onTouchRotate(event.x - prevX, event.y - prevY)
                    prevX = event.x; prevY = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mode == 3 && distFromStart(event) < 10f) {
                    if (System.currentTimeMillis() - startTime > 500) {
                        removeBlock(event.x, event.y, v.width, v.height)
                    } else {
                        placeBlock(event.x, event.y, v.width, v.height)
                    }
                }
                mode = 0; wasPinching = false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == pid2) {
                    wasPinching = false
                    prevX = event.getX(event.findPointerIndex(pid1))
                    prevY = event.getY(event.findPointerIndex(pid1))
                    mode = 3
                }
            }
            MotionEvent.ACTION_CANCEL -> { mode = 0; wasPinching = false }
        }
        return true
    }

    private fun distFromStart(e: MotionEvent): Float { val dx=e.x-startX; val dy=e.y-startY; return sqrt(dx*dx+dy*dy) }
    private fun dist(x1:Float,y1:Float,x2:Float,y2:Float): Float { val dx=x1-x2; val dy=y1-y2; return sqrt(dx*dx+dy*dy) }

    private fun placeBlock(sx: Float, sy: Float, vw: Int, vh: Int) {
        val ray = renderer.camera.screenToRay(sx, sy, vw.toFloat(), vh.toFloat())
        val hit = Raycaster.cast(world, ray) ?: return
        val bx=hit.faceX; val by=hit.faceY; val bz=hit.faceZ
        if (bx in 0 until world.width && by in 0 until world.height && bz in 0 until world.depth) {
            val existing = world.getBlock(bx, by, bz)
            if (existing == BlockType.AIR || existing == BlockType.WATER) {
                world.setBlock(bx, by, bz, selectedBlockType)
                onBlockChanged()
            }
        }
    }

    private fun removeBlock(sx: Float, sy: Float, vw: Int, vh: Int) {
        val ray = renderer.camera.screenToRay(sx, sy, vw.toFloat(), vh.toFloat())
        val hit = Raycaster.cast(world, ray) ?: return
        world.removeBlock(hit.blockX, hit.blockY, hit.blockZ)
        onBlockChanged()
    }
}
