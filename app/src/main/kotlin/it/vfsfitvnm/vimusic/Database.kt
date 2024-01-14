package it.vfsfitvnm.vimusic

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.os.Parcel
import androidx.core.database.getFloatOrNull
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.ArtistSortBy
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.SongWithContentLength
import it.vfsfitvnm.vimusic.models.Event
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Lyrics
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.PlaylistWithSongs
import it.vfsfitvnm.vimusic.models.QueuedMediaItem
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongAlbumMap
import it.vfsfitvnm.vimusic.models.SongArtistMap
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.models.SortedSongPlaylistMap
import it.vfsfitvnm.vimusic.service.LOCAL_KEY_PREFIX
import kotlin.jvm.Throws
import kotlinx.coroutines.flow.Flow

@Dao
interface Database {
    companion object : Database by DatabaseInitializer.Instance.database

    @Transaction
    @Query("SELECT * FROM Song WHERE id in (:idsList) ")
    @RewriteQueriesToDropUnusedColumns
    fun getSongsList(idsList: List<String>): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE ROWID='wooowww' ")
    @RewriteQueriesToDropUnusedColumns
    fun fakeSongsList(): Flow<List<Song>>

    @Transaction
    @Query("SELECT Playlist.*, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = Playlist.id) as songCount " +
            "FROM Song JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "JOIN Event ON Song.id = Event.songId JOIN Playlist ON Playlist.id = SongPlaylistMap.playlistId " +
            "WHERE Event.timestamp BETWEEN :from AND :to GROUP BY Playlist.id ORDER BY Event.timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun playlistsMostPlayedByPeriod(from: Long,to: Long, limit:Int): Flow<List<PlaylistPreview>>
    @Transaction
    @Query("SELECT Album.* FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "JOIN Event ON Song.id = Event.songId JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE Event.timestamp BETWEEN :from AND :to GROUP BY Album.id ORDER BY Event.timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun albumsMostPlayedByPeriod(from: Long,to: Long, limit:Int): Flow<List<Album>>

    @Transaction
    @Query("SELECT Artist.* FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId " +
            "JOIN Event ON Song.id = Event.songId JOIN Artist ON Artist.id = SongArtistMap.artistId " +
            "WHERE Event.timestamp BETWEEN :from AND :to GROUP BY Artist.id ORDER BY Event.timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun artistsMostPlayedByPeriod(from: Long,to: Long, limit:Int): Flow<List<Artist>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE timestamp " +
            "BETWEEN :from AND :to GROUP BY songId  ORDER BY timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun songsMostPlayedByPeriod(from: Long, to: Long, limit:Long = Long.MAX_VALUE): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY totalPlayTimeMs")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByPlayTimeAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY totalPlayTimeMs DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByPlayTimeDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY title")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByTitleAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY title DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByTitleDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY ROWID")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByRowIdAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByRowIdDesc(): Flow<List<Song>>


    fun songsFavorites(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<Song>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByPlayTimeAsc()
                SortOrder.Descending -> songsFavoritesByPlayTimeDesc()
            }
            SongSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByTitleAsc()
                SortOrder.Descending -> songsFavoritesByTitleDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByRowIdAsc()
                SortOrder.Descending -> songsFavoritesByRowIdDesc()
            }
        }
    }

    @Transaction
    //@Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE songId = :songId and (contentLength = totalPlayTimeMs)")
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE songId = :songId")
    fun songCached(songId: String): Flow<SongWithContentLength?>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs")
    fun songsOfflineByPlayTimeAsc(): Flow<List<SongWithContentLength>>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs DESC")
    fun songsOfflineByPlayTimeDesc(): Flow<List<SongWithContentLength>>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.title")
    fun songsOfflineByTitleAsc(): Flow<List<SongWithContentLength>>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.title DESC")
    fun songsOfflineByTitleDesc(): Flow<List<SongWithContentLength>>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID")
    fun songsOfflineByRowIdAsc(): Flow<List<SongWithContentLength>>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    fun songsOfflineByRowIdDesc(): Flow<List<SongWithContentLength>>

    fun songsOffline(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<SongWithContentLength>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByPlayTimeAsc()
                SortOrder.Descending -> songsOfflineByPlayTimeDesc()
            }
            SongSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByTitleAsc()
                SortOrder.Descending -> songsOfflineByTitleDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByRowIdAsc()
                SortOrder.Descending -> songsOfflineByRowIdDesc()
            }
        }
    }


    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID ASC")
    //@Query("SELECT * FROM Song ORDER BY ROWID ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY ROWID DESC")
    //@Query("SELECT * FROM Song ORDER BY ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY title ASC")
    //@Query("SELECT * FROM Song ORDER BY title ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY title DESC")
    //@Query("SELECT * FROM Song ORDER BY title DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleDesc(): Flow<List<Song>>

    @Transaction
//    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs ASC")
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 AND id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY totalPlayTimeMs ASC")
    //@Query("SELECT * FROM Song WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY totalPlayTimeMs ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeAsc(): Flow<List<Song>>

    @Transaction
//    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs DESC")
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 AND id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY totalPlayTimeMs DESC")
    //@Query("SELECT * FROM Song WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY totalPlayTimeMs DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeDesc(): Flow<List<Song>>

    fun songs(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<Song>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsByPlayTimeAsc()
                SortOrder.Descending -> songsByPlayTimeDesc()
            }
            SongSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> songsByTitleAsc()
                SortOrder.Descending -> songsByTitleDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsByRowIdAsc()
                SortOrder.Descending -> songsByRowIdDesc()
            }
        }
    }

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    @RewriteQueriesToDropUnusedColumns
    fun favorites(): Flow<List<Song>>

    @Query("SELECT * FROM QueuedMediaItem")
    fun queue(): List<QueuedMediaItem>

    @Query("DELETE FROM QueuedMediaItem")
    fun clearQueue()

    @Query("SELECT * FROM SearchQuery WHERE query LIKE :query ORDER BY id DESC")
    fun queries(query: String): Flow<List<SearchQuery>>

    @Query("SELECT COUNT (*) FROM SearchQuery")
    fun queriesCount(): Flow<Int>

    @Query("DELETE FROM SearchQuery")
    fun clearQueries()

    @Query("SELECT count(id) FROM Song WHERE id = :songId and likedAt is not NULL")
    fun songliked(songId: String): Int

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String): Flow<Song?>

    @Query("SELECT count(id) FROM Song WHERE id = :id")
    fun songExist(id: String): Int

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun likedAt(songId: String): Flow<Long?>

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun like(songId: String, likedAt: Long?): Int

    @Query("UPDATE Song SET durationText = :durationText WHERE id = :songId")
    fun updateDurationText(songId: String, durationText: String): Int

    @Query("SELECT * FROM Lyrics WHERE songId = :songId")
    fun lyrics(songId: String): Flow<Lyrics?>

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun artist(id: String): Flow<Artist?>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY name")
    fun preferitesArtistsByName(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY name DESC")
    fun artistsByNameDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY name ASC")
    fun artistsByNameAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt DESC")
    fun artistsByRowIdDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt ASC")
    fun artistsByRowIdAsc(): Flow<List<Artist>>

    fun artists(sortBy: ArtistSortBy, sortOrder: SortOrder): Flow<List<Artist>> {
        return when (sortBy) {
            ArtistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> artistsByNameAsc()
                SortOrder.Descending -> artistsByNameDesc()
            }
            ArtistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> artistsByRowIdAsc()
                SortOrder.Descending -> artistsByRowIdDesc()
            }
        }
    }

    @Query("SELECT * FROM Album WHERE id = :id")
    fun album(id: String): Flow<Album?>

    @Query("SELECT timestamp FROM Album WHERE id = :id")
    fun albumTimestamp(id: String): Long?

    @Transaction
    @Query("SELECT * FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = :albumId AND position IS NOT NULL ORDER BY position")
    @RewriteQueriesToDropUnusedColumns
    fun albumSongs(albumId: String): Flow<List<Song>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY title ASC")
    fun albumsByTitleAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY year ASC")
    fun albumsByYearAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt ASC")
    fun albumsByRowIdAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY title DESC")
    fun albumsByTitleDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY year DESC")
    fun albumsByYearDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt DESC")
    fun albumsByRowIdDesc(): Flow<List<Album>>

    fun albums(sortBy: AlbumSortBy, sortOrder: SortOrder): Flow<List<Album>> {
        return when (sortBy) {
            AlbumSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> albumsByTitleAsc()
                SortOrder.Descending -> albumsByTitleDesc()
            }
            AlbumSortBy.Year -> when (sortOrder) {
                SortOrder.Ascending -> albumsByYearAsc()
                SortOrder.Descending -> albumsByYearDesc()
            }
            AlbumSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> albumsByRowIdAsc()
                SortOrder.Descending -> albumsByRowIdDesc()
            }
        }
    }

    @Query("UPDATE Song SET totalPlayTimeMs = totalPlayTimeMs + :addition WHERE id = :id")
    fun incrementTotalPlayTimeMs(id: String, addition: Long)

    @Transaction
    @Query("SELECT max(position) FROM SongPlaylistMap WHERE playlistId = :id")
    fun getSongMaxPositionToPlaylist(id: Long): Int


    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT S.* FROM Song S INNER JOIN songplaylistmap SP ON S.id=SP.songId WHERE SP.playlistId=:id AND S.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY S.totalPlayTimeMs")
    fun SongsPlaylistByPlayTimeAsc(id: Long): Flow<List<Song>>

    @Transaction
    @Query("SELECT S.* FROM Song S INNER JOIN songplaylistmap SP ON S.id=SP.songId WHERE SP.playlistId=:id AND S.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY S.totalPlayTimeMs DESC")
    fun SongsPlaylistByPlayTimeDesc(id: Long): Flow<List<Song>>

    @Transaction
    @Query("SELECT SP.position FROM Song S INNER JOIN songplaylistmap SP ON S.id=SP.songId WHERE SP.playlistId=:id AND S.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY SP.position")
    fun SongsPlaylistMap(id: Long): Flow<List<Int>>

    fun SongsPlaylist(id: Long,sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<Song>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> SongsPlaylistByPlayTimeAsc(id)
                SortOrder.Descending -> SongsPlaylistByPlayTimeDesc(id)
            }
            SongSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> songsByTitleAsc()
                SortOrder.Descending -> songsByTitleDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsByRowIdAsc()
                SortOrder.Descending -> songsByRowIdDesc()
            }
            /*
            SongSortBy.Position -> when (sortOrder) {
                SortOrder.Ascending -> songsByRowIdAsc()
                SortOrder.Descending -> songsByRowIdDesc()
            }

             */
        }
    }

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name ASC")
    fun playlistPreviewsByNameAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID ASC")
    fun playlistPreviewsByDateAddedAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount ASC")
    fun playlistPreviewsByDateSongCountAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name DESC")
    fun playlistPreviewsByNameDesc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID DESC")
    fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreview>>
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount DESC")
    fun playlistPreviewsByDateSongCountDesc(): Flow<List<PlaylistPreview>>

    fun playlistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ): Flow<List<PlaylistPreview>> {
        return when (sortBy) {
            PlaylistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByNameAsc()
                SortOrder.Descending -> playlistPreviewsByNameDesc()
            }
            PlaylistSortBy.SongCount -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByDateSongCountAsc()
                SortOrder.Descending -> playlistPreviewsByDateSongCountDesc()
            }
            PlaylistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByDateAddedAsc()
                SortOrder.Descending -> playlistPreviewsByDateAddedDesc()
            }
        }
    }

    @Query("SELECT thumbnailUrl FROM Song JOIN SongPlaylistMap ON id = songId WHERE playlistId = :id ORDER BY position LIMIT 4")
    fun playlistThumbnailUrls(id: Long): Flow<List<String?>>

    @Transaction
    @Query("SELECT * FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId WHERE SongArtistMap.artistId = :artistId AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<Song>>

    @Query("SELECT * FROM Format WHERE songId = :songId")
    fun format(songId: String): Flow<Format?>

    @Query("SELECT contentLength FROM Format WHERE songId = :songId")
    fun formatContentLength(songId: String): Long

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    fun songsWithContentLength(): Flow<List<SongWithContentLength>>


    @Query("""
        UPDATE SongPlaylistMap SET position = 
          CASE 
            WHEN position < :fromPosition THEN position + 1
            WHEN position > :fromPosition THEN position - 1
            ELSE :toPosition
          END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) and MAX(:fromPosition,:toPosition)
    """)
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("DELETE FROM SongPlaylistMap WHERE playlistId = :id")
    fun clearPlaylist(id: Long)

    @Query("DELETE FROM SongAlbumMap WHERE albumId = :id")
    fun clearAlbum(id: String)

    @Query("SELECT loudnessDb FROM Format WHERE songId = :songId")
    fun loudnessDb(songId: String): Flow<Float?>

    @Query("SELECT * FROM Song WHERE title LIKE :query OR artistsText LIKE :query")
    fun search(query: String): Flow<List<Song>>

    @Query("SELECT albumId AS id, NULL AS name FROM SongAlbumMap WHERE songId = :songId")
    fun songAlbumInfo(songId: String): Info

    @Query("SELECT id, name FROM Artist LEFT JOIN SongArtistMap ON id = artistId WHERE songId = :songId")
    fun songArtistInfo(songId: String): List<Info>

/*
    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId GROUP BY songId ORDER BY SUM(CAST(playTime AS REAL) / (((:now - timestamp) / 86400000) + 1)) DESC LIMIT 1")
//    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId GROUP BY songId ORDER BY timestamp DESC LIMIT 1")
    @RewriteQueriesToDropUnusedColumns
    fun trending(now: Long = System.currentTimeMillis()): Flow<Song?>
//    fun trending(): Flow<Song?>
 */

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun trending(limit: Int = 3): Flow<List<Song>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun lastPlayed( limit: Int = 3 ): Flow<List<Song>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE (:now - Event.timestamp) <= :period AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun trending(
        limit: Int = 3,
        now: Long = System.currentTimeMillis(),
        period: Long
    ): Flow<List<Song>>

    @Query("SELECT COUNT (*) FROM Event")
    fun eventsCount(): Flow<Int>

    @Query("DELETE FROM Event")
    fun clearEvents()

    @Query("DELETE FROM Event WHERE songId = :songId")
    fun clearEventsFor(songId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Throws(SQLException::class)
    fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(format: Format)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songPlaylistMap: SongPlaylistMap): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(songArtistMap: SongArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(queuedMediaItems: List<QueuedMediaItem>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSongPlaylistMaps(songPlaylistMaps: List<SongPlaylistMap>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: Album, songAlbumMap: SongAlbumMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artists: List<Artist>, songArtistMaps: List<SongArtistMap>)

    @Transaction
    fun insert(mediaItem: MediaItem, block: (Song) -> Song = { it }) {
        val song = Song(
            id = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title!!.toString(),
            artistsText = mediaItem.mediaMetadata.artist?.toString(),
            durationText = mediaItem.mediaMetadata.extras?.getString("durationText"),
            thumbnailUrl = mediaItem.mediaMetadata.artworkUri?.toString()
        ).let(block).also { song ->
            if (insert(song) == -1L) return
        }

        mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            insert(
                Album(id = albumId, title = mediaItem.mediaMetadata.albumTitle?.toString()),
                SongAlbumMap(songId = song.id, albumId = albumId, position = null)
            )
        }

        mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                if (artistNames.size == artistIds.size) {
                    insert(
                        artistNames.mapIndexed { index, artistName ->
                            Artist(id = artistIds[index], name = artistName)
                        },
                        artistIds.map { artistId ->
                            SongArtistMap(songId = song.id, artistId = artistId)
                        }
                    )
                }
            }
        }
    }

    @Update
    fun update(artist: Artist)

    @Update
    fun update(album: Album)

    @Update
    fun update(playlist: Playlist)

    @Upsert
    fun upsert(lyrics: Lyrics)

    @Upsert
    fun upsert(album: Album, songAlbumMaps: List<SongAlbumMap>)

    @Upsert
    fun upsert(songAlbumMap: SongAlbumMap)

    @Upsert
    fun upsert(artist: Artist)

    @Upsert
    fun upsert(format: Format)

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMap)

    @RawQuery
    fun raw(supportSQLiteQuery: SupportSQLiteQuery): Int

    fun checkpoint() {
        raw(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
    }
}

@androidx.room.Database(
    entities = [
        Song::class,
        SongPlaylistMap::class,
        Playlist::class,
        Artist::class,
        SongArtistMap::class,
        Album::class,
        SongAlbumMap::class,
        SearchQuery::class,
        QueuedMediaItem::class,
        Format::class,
        Event::class,
        Lyrics::class,
    ],
    views = [
        SortedSongPlaylistMap::class
    ],
    version = 23,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = DatabaseInitializer.From3To4Migration::class),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8, spec = DatabaseInitializer.From7To8Migration::class),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 11, to = 12, spec = DatabaseInitializer.From11To12Migration::class),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21, spec = DatabaseInitializer.From20To21Migration::class),
        AutoMigration(from = 21, to = 22, spec = DatabaseInitializer.From21To22Migration::class),
    ],
)
@TypeConverters(Converters::class)
abstract class DatabaseInitializer protected constructor() : RoomDatabase() {
    abstract val database: Database

    companion object {
        lateinit var Instance: DatabaseInitializer

        context(Context)
        operator fun invoke() {
            if (!::Instance.isInitialized) {
                Instance = Room
                    .databaseBuilder(this@Context, DatabaseInitializer::class.java, "data.db")
                    .addMigrations(
                        From8To9Migration(),
                        From10To11Migration(),
                        From14To15Migration(),
                        From22To23Migration()
                    )
                    .build()
            }
        }
    }

    @DeleteTable.Entries(DeleteTable(tableName = "QueuedMediaItem"))
    class From3To4Migration : AutoMigrationSpec

    @RenameColumn.Entries(RenameColumn("Song", "albumInfoId", "albumId"))
    class From7To8Migration : AutoMigrationSpec

    class From8To9Migration : Migration(8, 9) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.query(SimpleSQLiteQuery("SELECT DISTINCT browseId, text, Info.id FROM Info JOIN Song ON Info.id = Song.albumId;"))
                .use { cursor ->
                    val albumValues = ContentValues(2)
                    while (cursor.moveToNext()) {
                        albumValues.put("id", cursor.getString(0))
                        albumValues.put("title", cursor.getString(1))
                        it.insert("Album", CONFLICT_IGNORE, albumValues)

                        it.execSQL(
                            "UPDATE Song SET albumId = '${cursor.getString(0)}' WHERE albumId = ${
                                cursor.getLong(
                                    2
                                )
                            }"
                        )
                    }
                }

            it.query(SimpleSQLiteQuery("SELECT GROUP_CONCAT(text, ''), SongWithAuthors.songId FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId GROUP BY songId;"))
                .use { cursor ->
                    val songValues = ContentValues(1)
                    while (cursor.moveToNext()) {
                        songValues.put("artistsText", cursor.getString(0))
                        it.update(
                            "Song",
                            CONFLICT_IGNORE,
                            songValues,
                            "id = ?",
                            arrayOf(cursor.getString(1))
                        )
                    }
                }

            it.query(SimpleSQLiteQuery("SELECT browseId, text, Info.id FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId WHERE browseId NOT NULL;"))
                .use { cursor ->
                    val artistValues = ContentValues(2)
                    while (cursor.moveToNext()) {
                        artistValues.put("id", cursor.getString(0))
                        artistValues.put("name", cursor.getString(1))
                        it.insert("Artist", CONFLICT_IGNORE, artistValues)

                        it.execSQL(
                            "UPDATE SongWithAuthors SET authorInfoId = '${cursor.getString(0)}' WHERE authorInfoId = ${
                                cursor.getLong(
                                    2
                                )
                            }"
                        )
                    }
                }

            it.execSQL("INSERT INTO SongArtistMap(songId, artistId) SELECT songId, authorInfoId FROM SongWithAuthors")

            it.execSQL("DROP TABLE Info;")
            it.execSQL("DROP TABLE SongWithAuthors;")
        }
    }

    class From10To11Migration : Migration(10, 11) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.query(SimpleSQLiteQuery("SELECT id, albumId FROM Song;")).use { cursor ->
                val songAlbumMapValues = ContentValues(2)
                while (cursor.moveToNext()) {
                    songAlbumMapValues.put("songId", cursor.getString(0))
                    songAlbumMapValues.put("albumId", cursor.getString(1))
                    it.insert("SongAlbumMap", CONFLICT_IGNORE, songAlbumMapValues)
                }
            }

            it.execSQL("CREATE TABLE IF NOT EXISTS `Song_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT NOT NULL, `thumbnailUrl` TEXT, `lyrics` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, `loudnessDb` REAL, `contentLength` INTEGER, PRIMARY KEY(`id`))")

            it.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs, loudnessDb, contentLength) SELECT id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs, loudnessDb, contentLength FROM Song;")
            it.execSQL("DROP TABLE Song;")
            it.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }

    @RenameTable("SongInPlaylist", "SongPlaylistMap")
    @RenameTable("SortedSongInPlaylist", "SortedSongPlaylistMap")
    class From11To12Migration : AutoMigrationSpec

    class From14To15Migration : Migration(14, 15) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.query(SimpleSQLiteQuery("SELECT id, loudnessDb, contentLength FROM Song;"))
                .use { cursor ->
                    val formatValues = ContentValues(3)
                    while (cursor.moveToNext()) {
                        formatValues.put("songId", cursor.getString(0))
                        formatValues.put("loudnessDb", cursor.getFloatOrNull(1))
                        formatValues.put("contentLength", cursor.getFloatOrNull(2))
                        it.insert("Format", CONFLICT_IGNORE, formatValues)
                    }
                }

            it.execSQL("CREATE TABLE IF NOT EXISTS `Song_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT NOT NULL, `thumbnailUrl` TEXT, `lyrics` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")

            it.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs) SELECT id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs FROM Song;")
            it.execSQL("DROP TABLE Song;")
            it.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }

    @DeleteColumn.Entries(
        DeleteColumn("Artist", "shuffleVideoId"),
        DeleteColumn("Artist", "shufflePlaylistId"),
        DeleteColumn("Artist", "radioVideoId"),
        DeleteColumn("Artist", "radioPlaylistId"),
    )
    class From20To21Migration : AutoMigrationSpec

    @DeleteColumn.Entries(DeleteColumn("Artist", "info"))
    class From21To22Migration : AutoMigrationSpec

    class From22To23Migration : Migration(22, 23) {
        override fun migrate(it: SupportSQLiteDatabase) {
            it.execSQL("CREATE TABLE IF NOT EXISTS Lyrics (`songId` TEXT NOT NULL, `fixed` TEXT, `synced` TEXT, PRIMARY KEY(`songId`), FOREIGN KEY(`songId`) REFERENCES `Song`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")

            it.query(SimpleSQLiteQuery("SELECT id, lyrics, synchronizedLyrics FROM Song;")).use { cursor ->
                val lyricsValues = ContentValues(3)
                while (cursor.moveToNext()) {
                    lyricsValues.put("songId", cursor.getString(0))
                    lyricsValues.put("fixed", cursor.getString(1))
                    lyricsValues.put("synced", cursor.getString(2))
                    it.insert("Lyrics", CONFLICT_IGNORE, lyricsValues)
                }
            }

            it.execSQL("CREATE TABLE IF NOT EXISTS Song_new (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT, `thumbnailUrl` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            it.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, likedAt, totalPlayTimeMs) SELECT id, title, artistsText, durationText, thumbnailUrl, likedAt, totalPlayTimeMs FROM Song;")
            it.execSQL("DROP TABLE Song;")
            it.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }
}

@TypeConverters
object Converters {

    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        return stringListString.split(",").map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = ",")
    }

    @TypeConverter
    @UnstableApi
    fun mediaItemFromByteArray(value: ByteArray?): MediaItem? {
        return value?.let { byteArray ->
            runCatching {
                val parcel = Parcel.obtain()
                parcel.unmarshall(byteArray, 0, byteArray.size)
                parcel.setDataPosition(0)
                val bundle = parcel.readBundle(MediaItem::class.java.classLoader)
                parcel.recycle()

                bundle?.let(MediaItem.CREATOR::fromBundle)
            }.getOrNull()
        }
    }

    @TypeConverter
    @UnstableApi
    fun mediaItemToByteArray(mediaItem: MediaItem?): ByteArray? {
        return mediaItem?.toBundle()?.let { persistableBundle ->
            val parcel = Parcel.obtain()
            parcel.writeBundle(persistableBundle)
            val bytes = parcel.marshall()
            parcel.recycle()

            bytes
        }
    }
}

val Database.internal: RoomDatabase
    get() = DatabaseInitializer.Instance

fun query(block: () -> Unit) = DatabaseInitializer.Instance.queryExecutor.execute(block)

fun transaction(block: () -> Unit) = with(DatabaseInitializer.Instance) {
    transactionExecutor.execute {
        runInTransaction(block)
    }
}

val RoomDatabase.path: String?
    get() = openHelper.writableDatabase.path
