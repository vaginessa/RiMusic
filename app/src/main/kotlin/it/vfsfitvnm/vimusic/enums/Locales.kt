package it.vfsfitvnm.vimusic.enums

enum class Languages {
    System,
    Arabic,
    Bashkir,
    Catalan,
    English,
    Esperanto,
    ChineseSimplified,
    ChineseTraditional,
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
            Arabic -> "ar"
            Bashkir -> "ba"
            Catalan -> "ca"
            ChineseSimplified -> "zh-CN"
            ChineseTraditional -> "zh-TW"
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