package it.vfsfitvnm.vimusic.enums

enum class Languages {
    English,
    Czeck,
    French,
    German,
    Italian,
    Romanian,
    Russian,
    Spain,
    Turkish;

    val code: String
        get() = when (this) {
            English -> "en"
            Italian -> "it"
            Czeck -> "cs"
            German -> "de"
            Spain -> "es"
            French -> "fr"
            Romanian -> "ro"
            Russian -> "ru"
            Turkish -> "tr"
        }
}