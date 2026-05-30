package com.blockvillage.app

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.blockvillage.app.input.TouchHandler
import com.blockvillage.app.renderer.BlockVillageRenderer
import com.blockvillage.app.storage.WorldStorage
import com.blockvillage.app.ui.BlockPalette
import com.blockvillage.app.world.World

class MainActivity : Activity() {

    private lateinit var glView: GLSurfaceView
    private lateinit var renderer: BlockVillageRenderer
    private lateinit var world: World
    private lateinit var touch: TouchHandler
    private lateinit var palette: BlockPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        world = World(64, 64, 32)

        if (WorldStorage.exists(this)) {
            WorldStorage.load(this, world)
            Toast.makeText(this, "World loaded!", Toast.LENGTH_SHORT).show()
        }

        val root = FrameLayout(this)

        glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            renderer = BlockVillageRenderer(world)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
        root.addView(glView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        touch = TouchHandler(renderer, world) { glView.requestRender() }
        glView.setOnTouchListener(touch)

        palette = BlockPalette(this).apply {
            onBlockSelected = { bt ->
                touch.selectedBlockType = bt
                Toast.makeText(this@MainActivity, "Selected: ${bt.label}", Toast.LENGTH_SHORT).show()
            }
        }
        val pp = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 16; leftMargin = 16; rightMargin = 16
        }
        root.addView(palette, pp)

        val info = TextView(this).apply {
            text = "🏡 BlockVillage — Tap: place | Long-press: remove | Drag: rotate | Pinch: zoom+pan"
            setTextColor(0xFFFFFFFF.toInt()); textSize = 12f
            setPadding(16, 12, 16, 12)
            setBackgroundColor(0xAA000000.toInt())
        }
        root.addView(info, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; topMargin = 16
        })

        setContentView(root)
    }

    override fun onPause() { super.onPause(); glView.onPause(); WorldStorage.save(this, world) }
    override fun onResume() { super.onResume(); glView.onResume() }
    override fun onDestroy() { super.onDestroy(); WorldStorage.save(this, world) }
}
