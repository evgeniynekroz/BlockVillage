package com.blockvillage.app.world

class World(val width: Int = 64, val depth: Int = 64, val height: Int = 32) {

    private val blocks = ByteArray(width * height * depth) { BlockType.AIR.id }
    private val dirtyChunks = mutableSetOf<Pair<Int, Int>>()
    val chunkSize = 16

    init {
        generateFlatWorld()
    }

    private fun generateFlatWorld() {
        val grassLevel = 4
        for (x in 0 until width) {
            for (z in 0 until depth) {
                setBlockRaw(x, grassLevel, z, BlockType.GRASS.id)
                for (y in grassLevel - 3 until grassLevel) {
                    setBlockRaw(x, y, z, BlockType.DIRT.id)
                }
                for (y in 0 until grassLevel - 3) {
                    setBlockRaw(x, y, z, BlockType.STONE.id)
                }
            }
        }
        // Small pond
        for (x in 28..32) {
            for (z in 28..32) {
                setBlockRaw(x, 4, z, BlockType.WATER.id)
                setBlockRaw(x, 3, z, BlockType.WATER.id)
            }
        }
        // Trees
        plantTree(10, 5, 10)
        plantTree(15, 5, 20)
        plantTree(50, 5, 15)
        plantTree(45, 5, 45)
        plantTree(35, 5, 50)
        plantTree(20, 5, 40)
    }

    private fun plantTree(x: Int, groundY: Int, z: Int) {
        for (y in groundY until groundY + 4) {
            setBlockRaw(x, y, z, BlockType.WOOD.id)
        }
        for (lx in -2..2) {
            for (lz in -2..2) {
                for (ly in groundY + 2..groundY + 4) {
                    if (kotlin.math.abs(lx) == 2 && kotlin.math.abs(lz) == 2 && ly == groundY + 4) continue
                    if (kotlin.math.abs(lx) + kotlin.math.abs(lz) <= 3) {
                        val bx = x + lx; val bz = z + lz
                        if (bx in 0 until width && bz in 0 until depth) {
                            if (getBlockRaw(bx, ly, bz) == BlockType.AIR.id) {
                                setBlockRaw(bx, ly, bz, BlockType.LEAVES.id)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getBlock(x: Int, y: Int, z: Int): BlockType =
        if (x in 0 until width && y in 0 until height && z in 0 until depth)
            BlockType.fromId(getBlockRaw(x, y, z))
        else BlockType.AIR

    fun getBlockRaw(x: Int, y: Int, z: Int): Byte = blocks[index(x, y, z)]

    fun setBlock(x: Int, y: Int, z: Int, blockType: BlockType) {
        if (x in 0 until width && y in 0 until height && z in 0 until depth) {
            setBlockRaw(x, y, z, blockType.id)
            markDirty(x, z)
            markDirty(x - 1, z); markDirty(x + 1, z)
            markDirty(x, z - 1); markDirty(x, z + 1)
        }
    }

    private fun setBlockRaw(x: Int, y: Int, z: Int, blockId: Byte) {
        blocks[index(x, y, z)] = blockId
    }

    fun removeBlock(x: Int, y: Int, z: Int) {
        if (x in 0 until width && y in 0 until height && z in 0 until depth) {
            setBlockRaw(x, y, z, BlockType.AIR.id)
            markDirty(x, z)
            markDirty(x - 1, z); markDirty(x + 1, z)
            markDirty(x, z - 1); markDirty(x, z + 1)
        }
    }

    private fun markDirty(x: Int, z: Int) {
        val cx = x / chunkSize; val cz = z / chunkSize
        if (cx in 0 until (width / chunkSize) && cz in 0 until (depth / chunkSize))
            dirtyChunks.add(cx to cz)
    }

    fun getDirtyChunks(): Set<Pair<Int, Int>> = dirtyChunks.toSet()
    fun clearDirty() { dirtyChunks.clear() }

    private fun index(x: Int, y: Int, z: Int): Int = (x * height + y) * depth + z

    fun isBlockExposed(x: Int, y: Int, z: Int): Boolean {
        val block = getBlock(x, y, z)
        if (!block.isSolid && block != BlockType.WATER) return false
        return !isSolidNeighbor(x+1,y,z) || !isSolidNeighbor(x-1,y,z) ||
               !isSolidNeighbor(x,y+1,z) || !isSolidNeighbor(x,y-1,z) ||
               !isSolidNeighbor(x,y,z+1) || !isSolidNeighbor(x,y,z-1)
    }

    private fun isSolidNeighbor(x: Int, y: Int, z: Int): Boolean {
        if (x !in 0 until width || y !in 0 until height || z !in 0 until depth) return false
        val block = getBlock(x, y, z)
        return block.isSolid && !block.isTransparent
    }

    fun toByteArray(): ByteArray = blocks.copyOf()

    fun loadFromByteArray(data: ByteArray) {
        if (data.size == blocks.size) {
            data.copyInto(blocks)
            for (cx in 0 until (width / chunkSize))
                for (cz in 0 until (depth / chunkSize))
                    dirtyChunks.add(cx to cz)
        }
    }
}
