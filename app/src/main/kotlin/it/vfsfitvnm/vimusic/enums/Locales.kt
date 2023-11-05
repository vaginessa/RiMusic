package it.vfsfitvnm.vimusic.enums

enum class Languages {
    English,
    Czech,
    French,
    German,
    Italian,
    Romanian,
    Russian,
    Spanish,
    Turkish;

    val code: String
        get() = when (this) {
            English -> "en"
            Italian -> "it"
            Czech -> "cs"
            German -> "de"
            Spanish -> "es"
            French -> "fr"
            Romanian -> "ro"
            Russian -> "ru"
            Turkish -> "tr"
        }

}