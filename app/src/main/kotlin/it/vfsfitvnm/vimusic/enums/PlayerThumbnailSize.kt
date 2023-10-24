package it.vfsfitvnm.vimusic.enums

enum class PlayerThumbnailSize {
    `Small`,
    `Medium`,
    `Big`;

    val size: Int
        get() = when (this) {
            `Small` -> 80
            `Medium` -> 45
            `Big` -> 10
        }

/*
    val sizename: String
        get() = when (this) {
            `Small` -> "Small"
            `Medium` -> "Medium"
            `Big` -> "Big"
        }
 */

}
