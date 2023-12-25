package it.vfsfitvnm.innertube.models

import it.vfsfitvnm.innertube.Innertube
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val platform: String,
        val hl: String = "en",
        //val hl: String = Locale.getDefault().toLanguageTag(), //"en",
        //val hl: String = Innertube.localeHl,
        val visitorData: String = "CgtEUlRINDFjdm1YayjX1pSaBg%3D%3D",
        val androidSdkVersion: Int? = null,
        val userAgent: String? = null
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )

    companion object {
        /*
        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220918",
                platform = "DESKTOP",
            )
        )


        val DefaultAndroid = Context(
            client = Client(
                clientName = "ANDROID_MUSIC",
                clientVersion = "5.28.1",
                platform = "MOBILE",
                androidSdkVersion = 30,
                userAgent = "com.google.android.apps.youtube.music/5.28.1 (Linux; U; Android 11) gzip"
            )
        )
        */

        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220606.03.00",
                platform = "DESKTOP",
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
                visitorData = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"

            )
        )

        val DefaultWebWithLocale = Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220606.03.00",
                platform = "DESKTOP",
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
                visitorData = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
                hl = Locale.getDefault().toLanguageTag()

            )
        )

        val DefaultAndroid = Context(
            client = Client(
                clientName = "ANDROID_MUSIC",
                clientVersion = "5.01",
                platform = "MOBILE",
                visitorData = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI",
                androidSdkVersion = 30,
                userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
            )
        )

        val DefaultAgeRestrictionBypass = Context(
            client = Client(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                platform = "TV"
            )
        )
    }
}
