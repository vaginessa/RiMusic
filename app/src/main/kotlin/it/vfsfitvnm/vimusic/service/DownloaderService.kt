package it.vfsfitvnm.vimusic.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import it.vfsfitvnm.vimusic.R
import javax.inject.Inject

@UnstableApi
class DownloaderService: DownloadService(
    NOTIFICATION_ID,
    1000L,
    CHANNEL_ID,
    R.string.download,
    0) {

    lateinit var downloader: Downloader

    override fun getDownloadManager() = downloader.downloadManager

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    @SuppressLint("ResourceType")
    override fun getForegroundNotification(downloads: MutableList<Download>, notMetRequirements: Int): Notification =
        downloader.downloadNotificationHelper.buildProgressNotification(
            this,
            R.drawable.download,
            null,
            if (downloads.size == 1) Util.fromUtf8Bytes(downloads[0].request.data)
            else resources.getQuantityString(R.string.songs, downloads.size, downloads.size),
            downloads,
            notMetRequirements
        )

    class TerminalStateNotificationHelper(
        private val context: Context,
        private val notificationHelper: DownloadNotificationHelper,
        private var nextNotificationId: Int,
    ) : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?,
        ) {
            if (download.state == Download.STATE_FAILED) {
                val notification = notificationHelper.buildDownloadFailedNotification(
                    context,
                    R.drawable.alert_circle,
                    null,
                    Util.fromUtf8Bytes(download.request.data)
                )
                NotificationUtil.setNotification(context, nextNotificationId++, notification)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "download"
        const val NOTIFICATION_ID = 1
        const val JOB_ID = 1
    }

}