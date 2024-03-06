package it.vfsfitvnm.vimusic.enums

enum class PlayerPlayButtonType {
    Disabled,
    Default,
    Rectangular,
    CircularRibbed,
    Square;

    val height: Int
        get() = when (this) {
            Default, Disabled -> 60
            Rectangular -> 70
            CircularRibbed -> 90
            Square -> 75
        }

    val width: Int
        get() = when (this) {
            Default, Disabled -> 60
            Rectangular -> 110
            CircularRibbed -> 90
            Square -> 75

        }
}