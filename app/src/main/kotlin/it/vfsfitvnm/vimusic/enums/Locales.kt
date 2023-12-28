package it.vfsfitvnm.vimusic.enums

enum class Languages {
    System,
    Bashkir,
    English,
    Esperanto,
    Czech,
    French,
    FrenchEmo,
    German,
    Greek,
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
            Bashkir -> "ba"
            English -> "en"
            Esperanto -> "eo"
            Italian -> "it"
            Indonesian -> "in"
            Czech -> "cs"
            German -> "de"
            Greek -> "el"
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