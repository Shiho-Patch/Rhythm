package chromahub.rhythm.app.features.local.data.db.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import chromahub.rhythm.app.shared.data.model.Song

/**
 * Room entity for storing song data in the local database
 */
@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["albumId"]),
        Index(value = ["genre"]),
        Index(value = ["dateAdded"])
    ]
)
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: String,
    val duration: Long,
    val uriString: String, // Store URI as string
    val artworkUriString: String? = null,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val albumArtist: String? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
    val codec: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
) {
    /**
     * Converts entity to domain model
     */
    fun toSong(): Song {
        return try {
            Song(
                id = id,
                title = title,
                artist = artist,
                album = album,
                albumId = albumId,
                duration = duration,
                uri = Uri.parse(uriString),
                artworkUri = artworkUriString?.let { uriStr ->
                    try {
                        Uri.parse(uriStr)
                    } catch (e: Exception) {
                        android.util.Log.w("SongEntity", "Invalid artwork URI: $uriStr for song: $title", e)
                        null
                    }
                },
                trackNumber = trackNumber,
                year = year,
                genre = genre,
                dateAdded = dateAdded,
                albumArtist = albumArtist,
                bitrate = bitrate,
                sampleRate = sampleRate,
                channels = channels,
                codec = codec
            )
        } catch (e: Exception) {
            android.util.Log.e("SongEntity", "Failed to convert entity to Song - ID: $id, Title: $title", e)
            throw IllegalStateException("Corrupted song entity in database: $id", e)
        }
    }

    companion object {
        /**
         * Converts domain model to entity
         */
        fun fromSong(song: Song, isFavorite: Boolean = false, playCount: Int = 0, lastPlayed: Long = 0L): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                albumId = song.albumId,
                duration = song.duration,
                uriString = song.uri.toString(),
                artworkUriString = song.artworkUri?.toString(),
                trackNumber = song.trackNumber,
                year = song.year,
                genre = song.genre,
                dateAdded = song.dateAdded,
                albumArtist = song.albumArtist,
                bitrate = song.bitrate,
                sampleRate = song.sampleRate,
                channels = song.channels,
                codec = song.codec,
                isFavorite = isFavorite,
                playCount = playCount,
                lastPlayed = lastPlayed
            )
        }
    }
}
