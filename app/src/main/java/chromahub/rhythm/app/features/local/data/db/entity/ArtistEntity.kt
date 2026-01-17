package chromahub.rhythm.app.features.local.data.db.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import chromahub.rhythm.app.shared.data.model.Artist
import chromahub.rhythm.app.shared.data.model.Album
import chromahub.rhythm.app.shared.data.model.Song

/**
 * Room entity for storing artist data
 */
@Entity(
    tableName = "artists",
    indices = [Index(value = ["name"])]
)
data class ArtistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val artworkUriString: String? = null,
    val numberOfAlbums: Int = 0,
    val numberOfTracks: Int = 0
) {
    /**
     * Converts entity to domain model
     * Note: albums and songs lists will need to be loaded separately
     */
    fun toArtist(albums: List<Album> = emptyList(), songs: List<Song> = emptyList()): Artist {
        return try {
            Artist(
                id = id,
                name = name,
                artworkUri = artworkUriString?.let { uriStr ->
                    try {
                        Uri.parse(uriStr)
                    } catch (e: Exception) {
                        android.util.Log.w("ArtistEntity", "Invalid artwork URI: $uriStr for artist: $name", e)
                        null
                    }
                },
                albums = albums,
                songs = songs,
                numberOfAlbums = numberOfAlbums,
                numberOfTracks = numberOfTracks
            )
        } catch (e: Exception) {
            android.util.Log.e("ArtistEntity", "Failed to convert entity to Artist - ID: $id, Name: $name", e)
            throw IllegalStateException("Corrupted artist entity in database: $id", e)
        }
    }

    companion object {
        /**
         * Converts domain model to entity
         */
        fun fromArtist(artist: Artist): ArtistEntity {
            return ArtistEntity(
                id = artist.id,
                name = artist.name,
                artworkUriString = artist.artworkUri?.toString(),
                numberOfAlbums = artist.albums.size,
                numberOfTracks = artist.songs.size
            )
        }
    }
}
