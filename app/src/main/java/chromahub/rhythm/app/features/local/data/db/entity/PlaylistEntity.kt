package chromahub.rhythm.app.features.local.data.db.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import chromahub.rhythm.app.features.local.data.db.converter.UriConverter
import chromahub.rhythm.app.shared.data.model.Playlist
import chromahub.rhythm.app.shared.data.model.Song
import chromahub.rhythm.app.util.GsonUtils
import com.google.gson.reflect.TypeToken

/**
 * Room entity for storing playlist data
 */
@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["name"]),
        Index(value = ["dateCreated"]),
        Index(value = ["dateModified"])
    ]
)
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val songIds: String, // JSON array of song IDs (safer than CSV for IDs with special chars)
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val artworkUriString: String? = null
) {
    /**
     * Converts entity to domain model
     * Note: songs list will need to be loaded separately using songIds
     */
    fun toPlaylist(songs: List<Song> = emptyList()): Playlist {
        return Playlist(
            id = id,
            name = name,
            songs = songs,
            dateCreated = dateCreated,
            dateModified = dateModified,
            artworkUri = artworkUriString?.let { Uri.parse(it) }
        )
    }

    companion object {
        /**
         * Converts domain model to entity
         */
        fun fromPlaylist(playlist: Playlist): PlaylistEntity {
            return PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                songIds = GsonUtils.gson.toJson(playlist.songs.map { it.id }),
                dateCreated = playlist.dateCreated,
                dateModified = playlist.dateModified,
                artworkUriString = playlist.artworkUri?.toString()
            )
        }
    }

    /**
     * Returns list of song IDs
     * Supports both JSON (new format) and CSV (legacy format) for backward compatibility
     */
    fun getSongIdsList(): List<String> {
        if (songIds.isEmpty()) return emptyList()
        
        return try {
            // Try JSON format first (new format)
            val type = object : TypeToken<List<String>>() {}.type
            val ids = GsonUtils.gson.fromJson<List<String>>(songIds, type)
            // Validate and filter out invalid IDs
            ids.filter { it.isNotBlank() && it.trim() == it }.also { validIds ->
                if (validIds.size != ids.size) {
                    android.util.Log.w("PlaylistEntity", "Filtered out ${ids.size - validIds.size} invalid song IDs for playlist $name")
                }
            }
        } catch (e: Exception) {
            android.util.Log.d("PlaylistEntity", "JSON parsing failed for playlist $name, trying CSV format", e)
            // Fallback to CSV format (legacy format)
            songIds.split(",").filter { it.isNotBlank() && it.trim() == it }.also { validIds ->
                if (validIds.isEmpty() && songIds.isNotBlank()) {
                    android.util.Log.w("PlaylistEntity", "Failed to parse any valid song IDs from: ${songIds.take(100)}")
                }
            }
        }
    }
}
