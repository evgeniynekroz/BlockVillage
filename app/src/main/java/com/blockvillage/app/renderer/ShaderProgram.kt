package com.blockvillage.app.renderer

import android.opengl.GLES20
import android.util.Log

class ShaderProgram(vertexSource: String, fragmentSource: String) {

    val programId: Int
    val aPosition: Int; val aTexCoord: Int; val aNormal: Int; val aColor: Int
    val uMVPMatrix: Int; val uModelMatrix: Int

    init {
        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
            val s = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, s, 0)
            if (s[0] == GLES20.GL_FALSE) {
                Log.e("Shader", "Link failed: ${GLES20.glGetProgramInfoLog(it)}")
                throw RuntimeException("Shader link failed")
            }
        }
        aPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        aTexCoord = GLES20.glGetAttribLocation(programId, "aTexCoord")
        aNormal = GLES20.glGetAttribLocation(programId, "aNormal")
        aColor = GLES20.glGetAttribLocation(programId, "aColor")
        uMVPMatrix = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        uModelMatrix = GLES20.glGetUniformLocation(programId, "uModelMatrix")
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
    }

    fun use() { GLES20.glUseProgram(programId) }

    private fun compileShader(type: Int, source: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, source)
        GLES20.glCompileShader(s)
        val st = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, st, 0)
        if (st[0] == GLES20.GL_FALSE) {
            Log.e("Shader", "Compile failed: ${GLES20.glGetShaderInfoLog(s)}")
            GLES20.glDeleteShader(s)
            throw RuntimeException("Shader compile failed")
        }
        return s
    }

    companion object {
        val VERTEX_SHADER = """
uniform mat4 uMVPMatrix;
uniform mat4 uModelMatrix;
attribute vec4 aPosition;
attribute vec2 aTexCoord;
attribute vec3 aNormal;
attribute vec3 aColor;
varying vec2 vTexCoord;
varying vec3 vColor;
varying float vLighting;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTexCoord = aTexCoord;
    vec3 worldNormal = normalize(mat3(uModelMatrix) * aNormal);
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.5));
    float ambient = 0.4;
    float diffuse = max(dot(worldNormal, lightDir), 0.0);
    vLighting = ambient + diffuse * 0.6;
    vColor = aColor;
}
""".trimIndent()

        val FRAGMENT_SHADER = """
precision mediump float;
varying vec2 vTexCoord;
varying vec3 vColor;
varying float vLighting;

void main() {
    gl_FragColor = vec4(vColor * vLighting, 1.0);
}
""".trimIndent()
    }
}
