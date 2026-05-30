package com.blockvillage.app.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.blockvillage.app.world.World
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class BlockVillageRenderer(val world: World) : GLSurfaceView.Renderer {

    private lateinit var shader: ShaderProgram
    val camera = Camera()
    private val modelMatrix = FloatArray(16)
    private val chunkMeshes = mutableMapOf<Pair<Int, Int>, MeshBuilder.Mesh>()
    private var vbo = 0; private var ebo = 0
    private var cv = 0; private var ci = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.53f, 0.81f, 0.98f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        shader = ShaderProgram(ShaderProgram.VERTEX_SHADER, ShaderProgram.FRAGMENT_SHADER)
        val bufs = IntArray(2)
        GLES20.glGenBuffers(2, bufs, 0)
        vbo = bufs[0]; ebo = bufs[1]
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        camera.setAspectRatio(width.toFloat() / height.toFloat())
    }

    override fun onDrawFrame(gl: GL10?) {
        for (chunk in world.getDirtyChunks()) {
            chunkMeshes[chunk] = MeshBuilder.buildWorldMesh(world, chunk.first, chunk.second)
        }
        world.clearDirty()

        val tv = mutableListOf<Float>(); val ti = mutableListOf<Short>()
        var bv = 0
        for ((chunk, mesh) in chunkMeshes) {
            if (mesh.vertexCount == 0) continue
            val va = FloatArray(mesh.vertexCount * 11)
            mesh.vertexBuffer.position(0); mesh.vertexBuffer.get(va)
            val ia = ShortArray(mesh.indexCount)
            mesh.indexBuffer.position(0); mesh.indexBuffer.get(ia)
            val cx = chunk.first * world.chunkSize; val cz = chunk.second * world.chunkSize
            for (v in 0 until mesh.vertexCount) {
                va[v*11] += cx.toFloat(); va[v*11+2] += cz.toFloat()
            }
            tv.addAll(va.toList())
            for (i in ia) ti.add((i + bv).toShort())
            bv += mesh.vertexCount
        }

        if (tv.isNotEmpty()) {
            val vb = ByteBuffer.allocateDirect(tv.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            vb.put(tv.toFloatArray()); vb.position(0)
            val ib = ByteBuffer.allocateDirect(ti.size*2).order(ByteOrder.nativeOrder()).asShortBuffer()
            ib.put(ti.toShortArray()); ib.position(0)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tv.size*4, vb, GLES20.GL_STATIC_DRAW)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo)
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ti.size*2, ib, GLES20.GL_STATIC_DRAW)
            cv = bv; ci = ti.size
        } else { cv = 0; ci = 0 }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (ci > 0) {
            shader.use()
            Matrix.setIdentityM(modelMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.uMVPMatrix, 1, false, camera.vpMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.uModelMatrix, 1, false, modelMatrix, 0)

            val stride = 44  // 11 floats * 4
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
            GLES20.glVertexAttribPointer(shader.aPosition, 3, GLES20.GL_FLOAT, false, stride, 0)
            GLES20.glEnableVertexAttribArray(shader.aPosition)
            GLES20.glVertexAttribPointer(shader.aTexCoord, 2, GLES20.GL_FLOAT, false, stride, 12)
            GLES20.glEnableVertexAttribArray(shader.aTexCoord)
            GLES20.glVertexAttribPointer(shader.aNormal, 3, GLES20.GL_FLOAT, false, stride, 20)
            GLES20.glEnableVertexAttribArray(shader.aNormal)
            GLES20.glVertexAttribPointer(shader.aColor, 3, GLES20.GL_FLOAT, false, stride, 32)
            GLES20.glEnableVertexAttribArray(shader.aColor)

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, ci, GLES20.GL_UNSIGNED_SHORT, 0)

            GLES20.glDisableVertexAttribArray(shader.aPosition)
            GLES20.glDisableVertexAttribArray(shader.aTexCoord)
            GLES20.glDisableVertexAttribArray(shader.aNormal)
            GLES20.glDisableVertexAttribArray(shader.aColor)
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun onTouchRotate(dx: Float, dy: Float) { camera.rotate(dx * 0.3f, -dy * 0.3f) }
    fun onTouchZoom(scale: Float) { camera.zoom(scale) }
    fun onTouchPan(dx: Float, dy: Float) { camera.pan(-dx, -dy) }
}
