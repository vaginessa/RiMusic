package it.vfsfitvnm.vimusic.enums

enum class PlayerPlayButtonType {
    Default,
    Rectangular,
    CircularRibbed,
    Square;

    val height: Int
        get() = when (this) {
            Default -> 60
            Rectangular -> 80
            CircularRibbed -> 90
            Square -> 80
        }

    val width: Int
        get() = when (this) {
            Default -> 60
            Rectangular -> 110
            CircularRibbed -> 90
            Square -> 80

        }
}