package chromahub.rhythm.app.features.local.data.db.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import chromahub.rhythm.app.shared.data.model.Album
import chromahub.rhythm.app.shared.data.model.Song

/**
 * Room entity for storing album data
 */
@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["year"])
    ]
)
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val artworkUriString: String? = null,
    val year: Int = 0,
    val numberOfSongs: Int = 0,
    val dateModified: Long = System.currentTimeMillis()
) {
    /**
     * Converts entity to domain model
     * Note: songs list will need to be loaded separately from SongEntity
     */
    fun toAlbum(songs: List<Song> = emptyList()): Album {
        return try {
            Album(
                id = id,
                title = title,
                artist = artist,
                artworkUri = artworkUriString?.let { uriStr ->
                    try {
                        Uri.parse(uriStr)
                    } catch (e: Exception) {
                        android.util.Log.w("AlbumEntity", "Invalid artwork URI: $uriStr for album: $title", e)
                        null
                    }
                },
                year = year,
                songs = songs,
                numberOfSongs = numberOfSongs,
                dateModified = dateModified
            )
        } catch (e: Exception) {
            android.util.Log.e("AlbumEntity", "Failed to convert entity to Album - ID: $id, Title: $title", e)
            throw IllegalStateException("Corrupted album entity in database: $id", e)
        }
    }

    companion object {
        /**
         * Converts domain model to entity
         */
        fun fromAlbum(album: Album): AlbumEntity {
            return AlbumEntity(
                id = album.id,
                title = album.title,
                artist = album.artist,
                artworkUriString = album.artworkUri?.toString(),
                year = album.year,
                numberOfSongs = album.songs.size,
                dateModified = album.dateModified
            )
        }
    }
}
