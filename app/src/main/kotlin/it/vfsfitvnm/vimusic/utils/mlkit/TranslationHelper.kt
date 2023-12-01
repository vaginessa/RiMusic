package it.vfsfitvnm.vimusic.utils.mlkit

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

@SuppressLint("SuspiciousIndentation")
@Composable
fun Translate (
    text: String,
    languageDestination: String
): String  {

    var languageSource by remember {
        mutableStateOf("")
    }
    //if (text.isNotEmpty() == true)
    //languageSource = IdentificationLanguage(text = text)
    languageSource = "en"

    var transText by remember {
        mutableStateOf("")
    }

val options = TranslatorOptions.Builder()
    .setSourceLanguage(languageSource)
    .setTargetLanguage(languageDestination)
    .build()

val languageTranslator = Translation.getClient(options)

var conditions = DownloadConditions.Builder()
    .requireWifi()
    .build()

languageTranslator.downloadModelIfNeeded(conditions)
.addOnSuccessListener {

    languageTranslator.translate(text)
        .addOnSuccessListener { translatedText ->
            transText = translatedText
            //Log.d("mediaItemTranslate","translate1 $transText")
        }
        .addOnFailureListener {
                //exception ->
            //Log.d("mediaItemTranslate","translate ERROR $exception")
        }

}
.addOnFailureListener {
       //exception ->
    // Model couldn’t be downloaded or other internal error.
    //Log.d("mediaItemTranslate","model not downloaded ERROR $exception")
}

    //Log.d("mediaItemTranslate","translate2 $transText")
    return transText
}

@Composable
fun IdentificationLanguage (
    text: String
): String {
    var langCode by remember {
        mutableStateOf("")
    }

    val languageIdentifier = LanguageIdentification.getClient()
    languageIdentifier.identifyLanguage(text)
        .addOnSuccessListener { languageCode ->
            if (languageCode == "und") {
                Log.i("mediaItemLangIdent", "Can't identify language.")
            } else {
                langCode = languageCode
                //Log.i("mediaItemLangIdent", "Language: $languageCode")
            }
        }
        .addOnFailureListener {
            // Model couldn’t be loaded or other internal error.
            // ...
        }
//    val targetLanguage = TranslateLanguage.fromLanguageTag(
//        Locale.getDefault().toLanguageTag().substring(0..1)
//    )

    return langCode

}