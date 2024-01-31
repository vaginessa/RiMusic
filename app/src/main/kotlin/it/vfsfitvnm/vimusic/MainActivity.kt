package it.vfsfitvnm.vimusic

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.BuildCompat
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil.Companion.getVersionCode
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import it.vfsfitvnm.compose.persist.PersistMap
import it.vfsfitvnm.compose.persist.PersistMapOwner
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.BrowseBody
import it.vfsfitvnm.innertube.requests.playlistPage
import it.vfsfitvnm.innertube.requests.song
import it.vfsfitvnm.innertube.utils.ProxyPreferenceItem
import it.vfsfitvnm.innertube.utils.ProxyPreferences
import it.vfsfitvnm.vimusic.enums.AudioQualityFormat
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.FontType
import it.vfsfitvnm.vimusic.enums.Languages
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.service.DownloadUtil
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.BottomSheetMenu
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.home.HomeScreen
import it.vfsfitvnm.vimusic.ui.screens.player.Player
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.styling.Appearance
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.colorPaletteOf
import it.vfsfitvnm.vimusic.ui.styling.dynamicColorPaletteOf
import it.vfsfitvnm.vimusic.ui.styling.typographyOf
import it.vfsfitvnm.vimusic.utils.InitDownloader
import it.vfsfitvnm.vimusic.utils.OkHttpRequest
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.applyFontPaddingKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.audioQualityFormatKey
import it.vfsfitvnm.vimusic.utils.closeWithBackButtonKey
import it.vfsfitvnm.vimusic.utils.colorPaletteModeKey
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.disablePlayerHorizontalSwipeKey
import it.vfsfitvnm.vimusic.utils.effectRotationKey
import it.vfsfitvnm.vimusic.utils.fontTypeKey
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid6
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid8
import it.vfsfitvnm.vimusic.utils.isKeepScreenOnEnabledKey
import it.vfsfitvnm.vimusic.utils.isProxyEnabledKey
import it.vfsfitvnm.vimusic.utils.languageAppKey
import it.vfsfitvnm.vimusic.utils.playerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.playerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.proxyHostnameKey
import it.vfsfitvnm.vimusic.utils.proxyModeKey
import it.vfsfitvnm.vimusic.utils.proxyPortKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerAddToPlaylistKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerArrowKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerDownloadKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerLoopKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerLyricsKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerShuffleKey
import it.vfsfitvnm.vimusic.utils.showLikeButtonBackgroundPlayerKey
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.useSystemFontKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.Proxy


@UnstableApi
class MainActivity : AppCompatActivity(), PersistMapOwner {

    var downloadUtil = DownloadUtil

    var client = OkHttpClient()
    var request = OkHttpRequest(client)

    var isConnected = false
    var updatedProductName = ""
    var updatedVersionName = ""
    var updatedVersionCode = 0



    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.Binder) {
                this@MainActivity.binder = service
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }

    private var binder by mutableStateOf<PlayerService.Binder?>(null)

    override lateinit var persistMap: PersistMap

    override fun onStart() {
        super.onStart()
        startService(Intent(this, PlayerService::class.java))
        bindService(intent<PlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    @ExperimentalMaterialApi
    @ExperimentalTextApi
    @UnstableApi
    //@androidx.annotation.OptIn(androidx.core.os.BuildCompat.PrereleaseSdkCheck::class)
    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var splashScreenStays = true
        val delayTime = 800L

        installSplashScreen().setKeepOnScreenCondition { splashScreenStays }
        Handler(Looper.getMainLooper()).postDelayed({ splashScreenStays = false }, delayTime)


        if (!preferences.getBoolean(closeWithBackButtonKey, false))
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                //Log.d("onBackPress", "yeah")
            }
        }


        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        persistMap = lastCustomNonConfigurationInstance as? PersistMap ?: PersistMap()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val launchedFromNotification = intent?.extras?.getBoolean("expandPlayerBottomSheet") == true

        with(preferences){
            if(getBoolean(isKeepScreenOnEnabledKey,false)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if(getBoolean(isProxyEnabledKey,false)) {
                val hostName = getString(proxyHostnameKey,null)
                val proxyPort = getInt(proxyPortKey, 8080)
                val proxyMode = getEnum(proxyModeKey, Proxy.Type.HTTP)
                hostName?.let { hName->
                    ProxyPreferences.preference = ProxyPreferenceItem(hName,proxyPort,proxyMode)
                }
            }
        }

        //Log.d("mediaItemLang",LocaleListCompat.getDefault().get(0).toString())
        //Innertube.localeHl = LocaleListCompat.getDefault().get(0).toString()
        //Log.d("mediaItemLang",LocaleListCompat.getDefault().get(0).toString()+" > "+Innertube.localeHl)

        setContent {

            val urlVersion = "https://raw.githubusercontent.com/fast4x/RiMusic/master/updatedVersion/updatedVersion.ver"
            val urlVersionCode = "https://raw.githubusercontent.com/fast4x/RiMusic/master/updatedVersion/updatedVersionCode.ver"
            //val urlVersionCode = "https://rimusic.xyz/update/updatedVersionCode.ver"

            request.GET(urlVersion, object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    runOnUiThread{
                        try {
                            isConnected = true
                            val newVersion = responseData.let { it.toString() }
                            val file = File(filesDir, "RiMusicUpdatedVersion.ver")
                            file.writeText(newVersion)

                            //this@MainActivity
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                }

                override fun onFailure(call: Call, e: java.io.IOException) {
                    Log.d("UpdatedVersion","Check failure")
                }
            })

            request.GET(urlVersionCode, object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    runOnUiThread{
                        try {
                            val json = responseData?.let { JSONObject(it) }
                            if (json != null) {
                                updatedProductName = json.getString("productName")
                                updatedVersionName = json.getString("versionName")
                                updatedVersionCode = json.getInt("versionCode")
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                }

                override fun onFailure(call: Call, e: java.io.IOException) {
                    Log.d("UpdatedVersionCode","Check failure")
                }
            })


            if (isConnected && updatedVersionCode > 0) {
                val manager = DownloadManager.Builder(this).run {
                    apkUrl("https://github.com/fast4x/RiMusic/releases/download/v${updatedVersionName}/app-release.apk")
                    //apkUrl("https://rimusic.xyz/update/v${BuildConfig.VERSION_NAME}/app-release.apk")
                    apkName("app-release.apk")
                    smallIcon(R.mipmap.ic_launcher)
                    //If this parameter is set, it will automatically determine whether to show dialog
                    apkVersionCode(updatedVersionCode) //with Int.MIN_VALUE start download immediately
                    apkVersionName(updatedVersionName)
                    //apkSize("5MB")
                    apkDescription(getString(R.string.update_now))
                    //Optional parameters...
                    showNewerToast(false)
                    enableLog(false)
                    jumpInstallPage(true)
                    //dialogButtonTextColor(Color.Green)
                    showNotification(true)
                    showBgdToast(false)
                    forcedUpgrade(false)
                    build()
                }
                manager.download()
            }


            val coroutineScope = rememberCoroutineScope()
            val isSystemInDarkTheme = isSystemInDarkTheme()

            preferences.getEnum(audioQualityFormatKey, AudioQualityFormat.Auto)

            var appearance by rememberSaveable(
                isSystemInDarkTheme,
                stateSaver = Appearance.Companion
            ) {
                with(preferences) {
                    val colorPaletteName = getEnum(colorPaletteNameKey, ColorPaletteName.ModernBlack)
                    val colorPaletteMode = getEnum(colorPaletteModeKey, ColorPaletteMode.System)
                    val thumbnailRoundness =
                        getEnum(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)
                    val useSystemFont = getBoolean(useSystemFontKey, false)
                    val applyFontPadding = getBoolean(applyFontPaddingKey, false)

                    val colorPalette =
                        colorPaletteOf(colorPaletteName, colorPaletteMode, isSystemInDarkTheme)

                    val fontType = getEnum(fontTypeKey, FontType.Rubik)

                    setSystemBarAppearance(colorPalette.isDark)

                    mutableStateOf(
                        Appearance(
                            colorPalette = colorPalette,
                            typography = typographyOf(colorPalette.text, useSystemFont, applyFontPadding, fontType),
                            thumbnailShape = thumbnailRoundness.shape()
                        )
                    )
                }

            }



            DisposableEffect(binder, isSystemInDarkTheme) {
                var bitmapListenerJob: Job? = null

                fun setDynamicPalette(colorPaletteMode: ColorPaletteMode) {
                    val isDark =
                        colorPaletteMode == ColorPaletteMode.Dark || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme)

                    binder?.setBitmapListener { bitmap: Bitmap? ->
                        if (bitmap == null) {
                            val colorPalette =
                                colorPaletteOf(
                                    ColorPaletteName.Dynamic,
                                    colorPaletteMode,
                                    isSystemInDarkTheme
                                )

                            setSystemBarAppearance(colorPalette.isDark)

                            appearance = appearance.copy(
                                colorPalette = colorPalette,
                                typography = appearance.typography.copy(colorPalette.text)
                            )

                            return@setBitmapListener
                        }

                        bitmapListenerJob = coroutineScope.launch(Dispatchers.IO) {
                            dynamicColorPaletteOf(bitmap, isDark)?.let {
                                withContext(Dispatchers.Main) {
                                    setSystemBarAppearance(it.isDark)
                                }
                                appearance = appearance.copy(
                                    colorPalette = it,
                                    typography = appearance.typography.copy(it.text)
                                )
                            }
                        }
                    }
                }

                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                        when (key) {

                            languageAppKey -> {
                                var lang = sharedPreferences.getEnum(
                                    languageAppKey,
                                    Languages.English
                                )

                                //val precLangCode = LocaleListCompat.getDefault().get(0).toString()
                                val systemLangCode = AppCompatDelegate.getApplicationLocales().get(0).toString()
                                //Log.d("LanguageActivity", "lang.code ${lang.code} precLangCode $precLangCode systemLangCode $systemLangCode")

                                val sysLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(systemLangCode)
                                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang.code)
                                AppCompatDelegate.setApplicationLocales( if (lang.code == "") sysLocale else appLocale )
                            }

                            effectRotationKey, playerThumbnailSizeKey,
                            //exoPlayerDiskCacheMaxSizeKey,
                            playerVisualizerTypeKey,
                            UiTypeKey,
                            disablePlayerHorizontalSwipeKey,
                            audioQualityFormatKey,
                            showButtonPlayerArrowKey,
                            showButtonPlayerAddToPlaylistKey,
                            showButtonPlayerDownloadKey,
                            showButtonPlayerLoopKey,
                            showButtonPlayerLyricsKey,
                            showButtonPlayerShuffleKey
                            -> {
                                this@MainActivity.recreate()
                            }

                            colorPaletteNameKey, colorPaletteModeKey -> {
                                val colorPaletteName =
                                    sharedPreferences.getEnum(
                                        colorPaletteNameKey,
                                        ColorPaletteName.Dynamic
                                    )

                                val colorPaletteMode =
                                    sharedPreferences.getEnum(
                                        colorPaletteModeKey,
                                        ColorPaletteMode.System
                                    )

                                if (colorPaletteName == ColorPaletteName.Dynamic) {
                                    setDynamicPalette(colorPaletteMode)
                                } else {
                                    bitmapListenerJob?.cancel()
                                    binder?.setBitmapListener(null)

                                    val colorPalette = colorPaletteOf(
                                        colorPaletteName,
                                        colorPaletteMode,
                                        isSystemInDarkTheme
                                    )

                                    setSystemBarAppearance(colorPalette.isDark)

                                    appearance = appearance.copy(
                                        colorPalette = colorPalette,
                                        typography = appearance.typography.copy(colorPalette.text),
                                    )
                                }
                            }

                            thumbnailRoundnessKey -> {
                                val thumbnailRoundness =
                                    sharedPreferences.getEnum(key, ThumbnailRoundness.Heavy)

                                appearance = appearance.copy(
                                    thumbnailShape = thumbnailRoundness.shape()
                                )
                            }

                            useSystemFontKey, applyFontPaddingKey, fontTypeKey -> {
                                val useSystemFont = sharedPreferences.getBoolean(useSystemFontKey, false)
                                val applyFontPadding = sharedPreferences.getBoolean(applyFontPaddingKey, false)
                                val fontType = sharedPreferences.getEnum(fontTypeKey, FontType.Rubik)

                                appearance = appearance.copy(
                                    typography = typographyOf(appearance.colorPalette.text, useSystemFont, applyFontPadding, fontType),
                                )
                            }
                        }
                    }

                with(preferences) {
                    registerOnSharedPreferenceChangeListener(listener)

                    val colorPaletteName = getEnum(colorPaletteNameKey, ColorPaletteName.ModernBlack)
                    if (colorPaletteName == ColorPaletteName.Dynamic) {
                        setDynamicPalette(getEnum(colorPaletteModeKey, ColorPaletteMode.System))
                    }

                    onDispose {
                        bitmapListenerJob?.cancel()
                        binder?.setBitmapListener(null)
                        unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }
            }

            val rippleTheme =
                remember(appearance.colorPalette.text, appearance.colorPalette.isDark) {
                    object : RippleTheme {
                        @Composable
                        override fun defaultColor(): Color = RippleTheme.defaultRippleColor(
                            contentColor = appearance.colorPalette.text,
                            lightTheme = !appearance.colorPalette.isDark
                        )

                        @Composable
                        override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
                            contentColor = appearance.colorPalette.text,
                            lightTheme = !appearance.colorPalette.isDark
                        )
                    }
                }

            val shimmerTheme = remember {
                defaultShimmerTheme.copy(
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = LinearEasing,
                            delayMillis = 250,
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    shaderColors = listOf(
                        Color.Unspecified.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.50f),
                        Color.Unspecified.copy(alpha = 0.25f),
                    ),
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appearance.colorPalette.background0)
            ) {
                val density = LocalDensity.current
                val windowsInsets = WindowInsets.systemBars
                val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

                val playerBottomSheetState = rememberBottomSheetState(
                    dismissedBound = 0.dp,
                    collapsedBound = Dimensions.collapsedPlayer + bottomDp,
                    expandedBound = maxHeight,
                )

                val playerAwareWindowInsets by remember(bottomDp, playerBottomSheetState.value) {
                    derivedStateOf {
                        val bottom = playerBottomSheetState.value.coerceIn(bottomDp, playerBottomSheetState.collapsedBound)

                        windowsInsets
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .add(WindowInsets(bottom = bottom))
                    }
                }


                CompositionLocalProvider(
                    LocalAppearance provides appearance,
                    LocalIndication provides rememberRipple(bounded = true),
                    LocalRippleTheme provides rippleTheme,
                    LocalShimmerTheme provides shimmerTheme,
                    LocalPlayerServiceBinder provides binder,
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                    LocalDownloader provides downloadUtil
                ) {



                    HomeScreen(
                        onPlaylistUrl = { url ->
                            onNewIntent(Intent.parseUri(url, 0))
                        }
                    )

                    Player(
                        layoutState = playerBottomSheetState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )

                    BottomSheetMenu(
                        state = LocalMenuState.current,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                }

                DisposableEffect(binder?.player) {
                    val player = binder?.player ?: return@DisposableEffect onDispose { }

                    if (player.currentMediaItem == null) {
                        if (!playerBottomSheetState.isDismissed) {
                            playerBottomSheetState.dismiss()
                        }
                    } else {
                        if (playerBottomSheetState.isDismissed) {
                            if (launchedFromNotification) {
                                intent.replaceExtras(Bundle())
                                playerBottomSheetState.expand(tween(700))
                            } else {
                                playerBottomSheetState.collapse(tween(700))
                            }
                        }
                    }

                    val listener = object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null) {
                                if (mediaItem.mediaMetadata.extras?.getBoolean("isFromPersistentQueue") != true) {
                                    playerBottomSheetState.expand(tween(500))
                                } else {
                                    playerBottomSheetState.collapse(tween(700))
                                }
                            }
                        }
                    }

                    player.addListener(listener)

                    onDispose { player.removeListener(listener) }
                }

                //VisualizerComputer.setupPermissions(this@MainActivity)
                //if (isConnected)
                InitDownloader()
            }
        }

        onNewIntent(intent)

    }
@UnstableApi
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

/*
               val action = intent.action
               val type = intent.type
               val data = intent.data

               Log.d("ShareActionInfo","Share action received action / type / data ${action} / ${type} / ${data}")
               if ("android.intent.action.SEND" == action && type != null && "text/plain" == type) {
                   Log.d("ShareActionTextExtra", intent.getStringExtra("android.intent.extra.TEXT")!!)
               }
*/

        val uri = intent.getStringExtra("android.intent.extra.TEXT")?.toUri() ?: return
        //val uri = intent?.data ?: return

        //intent.data = null
        //this.intent = null

        Toast.makeText(this, "${"RiMusic "}${getString(R.string.opening_url)}", Toast.LENGTH_LONG).show()

        lifecycleScope.launch(Dispatchers.IO) {
            when (val path = uri.pathSegments.firstOrNull()) {
                "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                    val browseId = "VL$playlistId"

                    if (playlistId.startsWith("OLAK5uy_")) {
                        Innertube.playlistPage(BrowseBody(browseId = browseId))?.getOrNull()?.let {
                            it.songsPage?.items?.firstOrNull()?.album?.endpoint?.browseId?.let { browseId ->
                                albumRoute.ensureGlobal(browseId)
                            }
                        }
                    } else {
                        //playlistRoute.ensureGlobal(browseId)
                        //playlistRoute.ensureGlobal(browseId, uri.getQueryParameter("params"))
                        playlistRoute.ensureGlobal(browseId,null)
                    }
                }

                "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                    artistRoute.ensureGlobal(channelId)
                }

                else -> when {
                    path == "watch" -> uri.getQueryParameter("v")
                    uri.host == "youtu.be" -> path
                    else -> null
                }?.let { videoId ->
                    Innertube.song(videoId)?.getOrNull()?.let { song ->
                        val binder = snapshotFlow { binder }.filterNotNull().first()
                        withContext(Dispatchers.Main) {
                            binder.player.forcePlay(song.asMediaItem)
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("persistMap"))
    override fun onRetainCustomNonConfigurationInstance() = persistMap

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()
        //stopService(Intent(this, MyDownloadService::class.java))
        //stopService(Intent(this, PlayerService::class.java))
        //Log.d("rimusic debug","onDestroy")
        if (!isChangingConfigurations) {
            persistMap.clear()
        }

    }

    private fun setSystemBarAppearance(isDark: Boolean) {
        with(WindowCompat.getInsetsController(window, window.decorView.rootView)) {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }

        if (!isAtLeastAndroid6) {
            window.statusBarColor =
                (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }

        if (!isAtLeastAndroid8) {
            window.navigationBarColor =
                (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
    }

}

val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerService.Binder?> { null }

val LocalPlayerAwareWindowInsets = staticCompositionLocalOf<WindowInsets> { TODO() }

val LocalDownloader = staticCompositionLocalOf<DownloadUtil> { error("No Downloader provided") }



