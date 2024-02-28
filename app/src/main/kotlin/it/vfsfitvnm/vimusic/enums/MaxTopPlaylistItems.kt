package it.vfsfitvnm.vimusic.enums

enum class MaxTopPlaylistItems {
    `10`,
    `20`,
    `30`,
    `40`,
    `50`,
    `70`,
    `90`,
    `100`,
    `150`,
    `200`;

    val number: Long
        get() = when (this) {
            `10` -> 10
            `20` -> 20
            `30` -> 30
            `40` -> 40
            `50` -> 50
            `70` -> 70
            `90` -> 90
            `100` -> 100
            `150` -> 150
            `200` -> 200
        }

}