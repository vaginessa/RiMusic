package it.vfsfitvnm.vimusic.enums

enum class Languages {
    English,
    Czech,
    French,
    FrenchEmo,
    German,
    Italian,
    Polish,
    Romanian,
    RomanianEmo,
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
            FrenchEmo -> "fr-FR"
            Romanian -> "ro"
            RomanianEmo -> "ro-RO"
            Russian -> "ru"
            Turkish -> "tr"
            Polish -> "pl"
        }


}