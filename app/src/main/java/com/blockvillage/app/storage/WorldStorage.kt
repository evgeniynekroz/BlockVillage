package com.blockvillage.app.storage

import android.content.Context
import com.blockvillage.app.world.World
import java.io.*

object WorldStorage {
    private const val FN = "blockvillage_world.dat"

    fun save(context: Context, world: World) {
        try {
            DataOutputStream(BufferedOutputStream(FileOutputStream(File(context.filesDir, FN)))).use { d ->
                val data = world.toByteArray()
                d.writeInt(world.width); d.writeInt(world.height); d.writeInt(world.depth)
                d.writeInt(data.size); d.write(data); d.flush()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun load(context: Context, world: World): Boolean {
        try {
            val f = File(context.filesDir, FN)
            if (!f.exists()) return false
            DataInputStream(BufferedInputStream(FileInputStream(f))).use { d ->
                val w=d.readInt(); val h=d.readInt(); val dp=d.readInt(); val sz=d.readInt()
                if (w==world.width && h==world.height && dp==world.depth) {
                    val data = ByteArray(sz); d.readFully(data)
                    world.loadFromByteArray(data); return true
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return false
    }

    fun exists(context: Context) = File(context.filesDir, FN).exists()
}
