package it.vfsfitvnm.vimusic.enums

enum class HomeScreenTabs {
    QuickPics,
    Songs,
    Artists,
    Albums,
    Library,
    Discovery;



    val index: Int
        get() = when (this) {
            QuickPics -> 0
            Songs -> 1
            Artists -> 2
            Albums -> 3
            Library -> 4
            Discovery -> 5

        }

}