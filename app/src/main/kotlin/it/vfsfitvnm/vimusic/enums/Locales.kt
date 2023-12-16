package it.vfsfitvnm.vimusic.enums

enum class Languages {
    System,
    English,
    Czech,
    French,
    FrenchEmo,
    German,
    Italian,
    Indonesian,
    Odia,
    Polish,
    PortugueseBrazilian,
    Portuguese,
    Romanian,
    RomanianEmo,
    Russian,
    Spanish,
    Turkish;

    var code: String = "en"
        get() = when (this) {
            System -> "system"
            English -> "en"
            Italian -> "it"
            Indonesian -> "in"
            Czech -> "cs"
            German -> "de"
            Spanish -> "es"
            French -> "fr"
            FrenchEmo -> "fr-FR"
            Odia -> "or"
            Polish -> "pl"
            Portuguese -> "pt"
            PortugueseBrazilian -> "pt-BR"
            Romanian -> "ro"
            RomanianEmo -> "ro-RO"
            Russian -> "ru"
            Turkish -> "tr"
        }


}