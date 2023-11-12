package it.vfsfitvnm.vimusic.enums

enum class Languages {
    English,
    Czech,
    French,
    German,
    Italian,
    Polish,
    Romanian,
    Russian,
    Spanish,
    Turkish;

    var code: String = "en"
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
            Polish -> "pl"
        }


}