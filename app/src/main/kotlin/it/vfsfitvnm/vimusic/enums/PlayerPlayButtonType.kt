package it.vfsfitvnm.vimusic.enums

enum class PlayerPlayButtonType {
    Default,
    Rectangular,
    CircularRibbed,
    Square;

    val height: Int
        get() = when (this) {
            Default -> 60
            Rectangular -> 90
            CircularRibbed -> 90
            Square -> 90
        }

    val width: Int
        get() = when (this) {
            Default -> 60
            Rectangular -> 120
            CircularRibbed -> 90
            Square -> 90

        }
}