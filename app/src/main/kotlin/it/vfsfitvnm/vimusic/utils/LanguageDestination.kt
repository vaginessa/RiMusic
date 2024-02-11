package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import it.vfsfitvnm.vimusic.enums.Languages
import me.bush.translator.Language

@Composable
fun languageDestination (): Language {
    val languageApp  by rememberPreference(languageAppKey, Languages.English)
    return when (languageApp) {
        Languages.Arabic -> Language.ARABIC
        Languages.Bashkir -> Language.BASQUE
        Languages.Catalan -> Language.CATALAN
        Languages.ChineseSimplified -> Language.CHINESE_SIMPLIFIED
        Languages.ChineseTraditional -> Language.CHINESE_TRADITIONAL
        Languages.Czech -> Language.CZECH
        Languages.Dutch -> Language.DUTCH
        Languages.English -> Language.ENGLISH
        Languages.Esperanto -> Language.ESPERANTO
        Languages.Finnish -> Language.FINNISH
        Languages.French -> Language.FRENCH
        Languages.German -> Language.GERMAN
        Languages.Greek -> Language.GREEK
        Languages.Hebrew -> Language.HEBREW_HE
        Languages.Hindi -> Language.HINDI
        Languages.Hungarian -> Language.HUNGARIAN
        Languages.Indonesian -> Language.INDONESIAN
        Languages.Japanese -> Language.JAPANESE
        Languages.Korean -> Language.KOREAN
        Languages.Italian -> Language.ITALIAN
        Languages.Odia -> Language.ODIA
        Languages.Persian -> Language.PERSIAN
        Languages.Polish -> Language.POLISH
        Languages.PortugueseBrazilian -> Language.PORTUGUESE
        Languages.Portuguese -> Language.PORTUGUESE
        Languages.Romanian -> Language.ROMANIAN
        Languages.Russian -> Language.RUSSIAN
        Languages.Sinhala -> Language.SINHALA
        Languages.Spanish -> Language.SPANISH
        Languages.Turkish -> Language.TURKISH
        Languages.Ukrainian -> Language.UKRAINIAN
        Languages.Vietnamese -> Language.VIETNAMESE
        else -> Language.ENGLISH
    }
}