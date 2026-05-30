package com.blockvillage.app.renderer

import com.blockvillage.app.world.BlockType
import com.blockvillage.app.world.World
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class MeshBuilder {

    data class Mesh(
        val vertexBuffer: FloatBuffer,
        val indexBuffer: ShortBuffer,
        val vertexCount: Int,
        val indexCount: Int
    )

    companion object {
        private const val FLOATS_PER_VERTEX = 11

        private val CUBE_VERTS = floatArrayOf(
            0f,0f,1f, 1f,0f,1f, 1f,1f,1f, 0f,1f,1f,
            1f,0f,0f, 0f,0f,0f, 0f,1f,0f, 1f,1f,0f,
            0f,1f,1f, 1f,1f,1f, 1f,1f,0f, 0f,1f,0f,
            0f,0f,0f, 1f,0f,0f, 1f,0f,1f, 0f,0f,1f,
            1f,0f,1f, 1f,0f,0f, 1f,1f,0f, 1f,1f,1f,
            0f,0f,0f, 0f,0f,1f, 0f,1f,1f, 0f,1f,0f
        )

        private val FACE_NORMALS = arrayOf(
            floatArrayOf(0f,0f,1f), floatArrayOf(0f,0f,-1f),
            floatArrayOf(0f,1f,0f), floatArrayOf(0f,-1f,0f),
            floatArrayOf(1f,0f,0f), floatArrayOf(-1f,0f,0f)
        )

        private val TEX_COORDS = floatArrayOf(0f,0f, 1f,0f, 1f,1f, 0f,1f)
        private val FACE_INDICES = shortArrayOf(0,1,2, 0,2,3)

        fun buildWorldMesh(world: World, chunkCx: Int, chunkCz: Int): Mesh {
            val cs = world.chunkSize
            val sx = chunkCx * cs; val sz = chunkCz * cs
            val ex = minOf(sx + cs, world.width); val ez = minOf(sz + cs, world.depth)

            val vList = mutableListOf<Float>()
            val iList = mutableListOf<Short>()
            var vc = 0

            fun addFace(bx: Float, by: Float, bz: Float, faceIdx: Int, block: BlockType) {
                val bp = CUBE_VERTS.copyOfRange(faceIdx * 12, faceIdx * 12 + 12)
                val n = FACE_NORMALS[faceIdx]
                val rgb = when {
                    block == BlockType.GRASS && faceIdx == 2 -> block.topColor
                    block == BlockType.GRASS && faceIdx == 3 -> block.bottomColor
                    block == BlockType.GRASS -> block.sideColor
                    faceIdx == 2 -> block.topColor
                    faceIdx == 3 -> block.bottomColor
                    else -> block.sideColor
                }
                val r = ((rgb shr 16) and 0xFF) / 255f
                val g = ((rgb shr 8) and 0xFF) / 255f
                val b = (rgb and 0xFF) / 255f
                for (v in 0 until 4) {
                    vList.add(bp[v*3] + bx); vList.add(bp[v*3+1] + by); vList.add(bp[v*3+2] + bz)
                    vList.add(TEX_COORDS[v*2]); vList.add(TEX_COORDS[v*2+1])
                    vList.add(n[0]); vList.add(n[1]); vList.add(n[2])
                    vList.add(r); vList.add(g); vList.add(b)
                }
                val base = vc.toShort()
                for (i in FACE_INDICES) iList.add((base + i).toShort())
                vc += 4
            }

            for (x in sx until ex) {
                for (z in sz until ez) {
                    for (y in 0 until world.height) {
                        val block = world.getBlock(x, y, z)
                        if (!block.isSolid && block != BlockType.WATER) continue
                        if (!world.isBlockExposed(x, y, z)) continue
                        val bx = x.toFloat(); val by = y.toFloat(); val bz = z.toFloat()

                        if (!(world.getBlock(x,y,z+1).let { it.isSolid && !it.isTransparent })) addFace(bx,by,bz,0,block)
                        if (!(world.getBlock(x,y,z-1).let { it.isSolid && !it.isTransparent })) addFace(bx,by,bz,1,block)
                        if (!(world.getBlock(x,y+1,z).let { it.isSolid && !it.isTransparent })) addFace(bx,by,bz,2,block)
                        if (!(world.getBlock(x,y-1,z).let { it.isSolid && !it.isTransparent })) addFace(bx,by,bz,3,block)
                        if (!(world.getBlock(x+1,y,z).let { it.isSolid && !it.isTransparent })) addFace(bx,by,bz,4,block)
                        if (!(world.getBlock(x-1,y,z).let { it.isSolid && !it.isTransparent })) addFace(bx,by,bz,5,block)
                    }
                }
            }

            val vb = ByteBuffer.allocateDirect(vList.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            vb.put(vList.toFloatArray()); vb.position(0)
            val ib = ByteBuffer.allocateDirect(iList.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
            ib.put(iList.toShortArray()); ib.position(0)
            return Mesh(vb, ib, vc, iList.size)
        }
    }
}
