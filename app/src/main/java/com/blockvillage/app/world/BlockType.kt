package com.blockvillage.app.world

enum class BlockType(
    val id: Byte,
    val label: String,
    val topColor: Int,
    val sideColor: Int,
    val bottomColor: Int,
    val isTransparent: Boolean = false,
    val isSolid: Boolean = true
) {
    AIR(0, "Air", 0, 0, 0, isSolid = false),
    GRASS(1, "Grass", 0xFF5D9B3A.toInt(), 0xFF8B6914.toInt(), 0xFF6B4E0A.toInt()),
    DIRT(2, "Dirt", 0xFF8B6914.toInt(), 0xFF8B6914.toInt(), 0xFF6B4E0A.toInt()),
    STONE(3, "Stone", 0xFF7F7F7F.toInt(), 0xFF7F7F7F.toInt(), 0xFF6A6A6A.toInt()),
    WOOD(4, "Wood", 0xFFBC9862.toInt(), 0xFF8B6914.toInt(), 0xFF6B4E0A.toInt()),
    LEAVES(5, "Leaves", 0xFF3B7A1E.toInt(), 0xFF3B7A1E.toInt(), 0xFF2D5F15.toInt(), isTransparent = true),
    PLANKS(6, "Planks", 0xFFBC9862.toInt(), 0xFFBC9862.toInt(), 0xFFA07840.toInt()),
    SAND(7, "Sand", 0xFFDCC98E.toInt(), 0xFFDCC98E.toInt(), 0xFFC4B078.toInt()),
    WATER(8, "Water", 0xFF3355CC.toInt(), 0xFF3355CC.toInt(), 0xFF2244AA.toInt(), isTransparent = true),
    BRICK(9, "Brick", 0xFFB53C2C.toInt(), 0xFFB53C2C.toInt(), 0xFF8B2818.toInt()),
    GLASS(10, "Glass", 0xFFAADDFF.toInt(), 0xFFAADDFF.toInt(), 0xFF88CCEE.toInt(), isTransparent = true),
    FLOWER(11, "Flower", 0xFF5D9B3A.toInt(), 0xFF2ECF3F.toInt(), 0xFF5D9B3A.toInt(), isTransparent = true);

    companion object {
        fun fromId(id: Byte): BlockType = values().find { it.id == id } ?: AIR
        val placeableBlocks = values().filter { it.isSolid && it != AIR }
    }
}
