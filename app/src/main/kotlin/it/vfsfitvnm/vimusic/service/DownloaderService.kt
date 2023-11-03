package it.vfsfitvnm.vimusic.service

import android.app.Notification
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import it.vfsfitvnm.vimusic.enums.ExoPlayerDiskCacheMaxSize
import it.vfsfitvnm.vimusic.utils.exoPlayerDiskCacheMaxSizeKey
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.preferences
import java.util.concurrent.Executor
@UnstableApi
class DownloaderService: DownloadService(FOREGROUND_NOTIFICATION_ID_NONE) {
    override fun getDownloadManager(): DownloadManager {
        // Note: This should be a singleton in your app.
        val databaseProvider = StandaloneDatabaseProvider(this)

// A download cache should not evict media, so should use a NoopCacheEvictor.
        val downloadDirectory = cacheDir
        val cacheEvictor = when (val size =
            preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`2GB`)) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(size.bytes)
        }
        val downloadCache = SimpleCache(downloadDirectory, cacheEvictor, databaseProvider)

// Create a factory for reading the data from the network.
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        

// Choose an executor for downloading data. Using Runnable::run will cause each download task to
// download data on its own thread. Passing an executor that uses multiple threads will speed up
// download tasks that can be split into smaller parts for parallel execution. Applications that
// already have an executor for background downloads may wish to reuse their existing executor.
        val downloadExecutor = Executor(Runnable::run)

// Create the download manager.
        val downloadManager =
            DownloadManager(applicationContext, databaseProvider, downloadCache, dataSourceFactory, downloadExecutor)

// Optionally, properties can be assigned to configure the download manager.
        //downloadManager.requirements = requirements
        downloadManager.maxParallelDownloads = 3

        return downloadManager
    }

    override fun getScheduler(): Scheduler? {
        TODO("Not yet implemented")
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        TODO("Not yet implemented")
    }

}