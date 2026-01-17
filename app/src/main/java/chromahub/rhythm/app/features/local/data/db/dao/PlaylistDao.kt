package chromahub.rhythm.app.features.local.data.db.dao

import androidx.room.*
import chromahub.rhythm.app.features.local.data.db.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Playlist operations
 */
@Dao
interface PlaylistDao {
    
    // ========== Query Operations ==========
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    suspend fun getAllPlaylists(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun getPlaylistByIdFlow(playlistId: String): Flow<PlaylistEntity?>
    
    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?
    
    @Query("SELECT * FROM playlists ORDER BY dateCreated DESC")
    suspend fun getPlaylistsByDateCreated(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists ORDER BY dateModified DESC")
    suspend fun getPlaylistsByDateModified(): List<PlaylistEntity>
    
    @Query("""
        SELECT * FROM playlists 
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun searchPlaylists(query: String): List<PlaylistEntity>
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
    
    // ========== Insert Operations ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)
    
    @Transaction
    suspend fun replaceAllPlaylists(playlists: List<PlaylistEntity>) {
        try {
            // Safety check with better logging
            if (playlists.isEmpty()) {
                android.util.Log.w("PlaylistDao", "Replacing all playlists with empty list - user may have deleted all custom playlists")
            }
            
            // Create backup before delete
            val currentPlaylists = getAllPlaylists()
            
            try {
                deleteAllPlaylists()
                insertPlaylists(playlists)
                android.util.Log.d("PlaylistDao", "Successfully replaced ${currentPlaylists.size} playlists with ${playlists.size} playlists")
            } catch (e: Exception) {
                // Rollback: restore old playlists
                android.util.Log.e("PlaylistDao", "Failed to replace playlists, attempting rollback", e)
                try {
                    deleteAllPlaylists()
                    insertPlaylists(currentPlaylists)
                    android.util.Log.d("PlaylistDao", "Rollback successful, restored ${currentPlaylists.size} playlists")
                } catch (rollbackError: Exception) {
                    android.util.Log.e("PlaylistDao", "CRITICAL: Rollback failed! Data may be lost", rollbackError)
                }
                throw e
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaylistDao", "Error in replaceAllPlaylists", e)
            throw e
        }
    }
    
    // ========== Update Operations ==========
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Query("UPDATE playlists SET name = :newName, dateModified = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: String, newName: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE playlists SET songIds = :songIds, dateModified = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistSongs(playlistId: String, songIds: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE playlists SET dateModified = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistModifiedDate(playlistId: String, timestamp: Long = System.currentTimeMillis())
    
    // ========== Delete Operations ==========
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)
    
    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()
    
    @Query("DELETE FROM playlists WHERE id NOT IN ('1', '2', '3')")
    suspend fun deleteNonDefaultPlaylists()
}
