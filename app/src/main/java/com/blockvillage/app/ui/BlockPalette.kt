package com.blockvillage.app.ui

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.blockvillage.app.world.BlockType

class BlockPalette(context: Context) : View(context) {

    private val cellSize = 72f; private val pad = 8f
    private val blocks = BlockType.placeableBlocks.toList()
    private var sel = 0
    var onBlockSelected: ((BlockType) -> Unit)? = null

    private val bgP = Paint().apply { color=0xCC222222.toInt(); style=Paint.Style.FILL }
    private val selP = Paint().apply { color=0xFFFFFFFF.toInt(); style=Paint.Style.STROKE; strokeWidth=4f }
    private val txtP = Paint().apply { color=Color.WHITE; textSize=18f; textAlign=Paint.Align.CENTER; isAntiAlias=true }
    private val blkP = Paint().apply { style=Paint.Style.FILL; isAntiAlias=true }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 12f, 12f, bgP)
        val tw = blocks.size * cellSize + (blocks.size-1) * pad + pad*2
        val sx = (width - tw)/2f + pad; val sy = pad

        for ((i, b) in blocks.withIndex()) {
            val x = sx + i*(cellSize+pad); val y = sy
            blkP.color = b.topColor
            canvas.drawRoundRect(x+6f, y+6f, x+cellSize-6f, y+cellSize-6f, 6f, 6f, blkP)
            if (i == sel) canvas.drawRoundRect(x+4f, y+4f, x+cellSize-4f, y+cellSize-4f, 8f, 8f, selP)
            canvas.drawText(b.label, x+cellSize/2f, y+cellSize+16f, txtP)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tw = blocks.size*cellSize + (blocks.size-1)*pad + pad*2
            val sx = (width-tw)/2f + pad
            val idx = ((event.x - sx) / (cellSize+pad)).toInt()
            if (idx in blocks.indices) { sel = idx; onBlockSelected?.invoke(blocks[idx]); invalidate() }
        }
        return true
    }

    fun getSelectedBlock(): BlockType = blocks[sel]

    override fun onMeasure(wms: Int, hms: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(wms),
            (cellSize + pad*2 + 28f).toInt()
        )
    }
}
