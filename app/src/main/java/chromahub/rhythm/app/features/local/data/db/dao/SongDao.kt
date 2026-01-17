package chromahub.rhythm.app.features.local.data.db.dao

import androidx.room.*
import chromahub.rhythm.app.features.local.data.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Song operations
 */
@Dao
interface SongDao {
    
    // ========== Query Operations ==========
    
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongs(): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: String): SongEntity?
    
    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber ASC")
    suspend fun getSongsByAlbum(albumId: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album ASC, trackNumber ASC")
    suspend fun getSongsByArtist(artist: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE genre = :genre ORDER BY title ASC")
    suspend fun getSongsByGenre(genre: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayed DESC")
    fun getFavoriteSongsFlow(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayed DESC")
    suspend fun getFavoriteSongs(): List<SongEntity>
    
    @Query("SELECT * FROM songs ORDER BY lastPlayed DESC LIMIT :limit")
    suspend fun getRecentlyPlayed(limit: Int = 20): List<SongEntity>
    
    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    suspend fun getMostPlayed(limit: Int = 20): List<SongEntity>
    
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentlyAdded(limit: Int = 20): List<SongEntity>
    
    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    suspend fun searchSongs(query: String): List<SongEntity>
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("SELECT COUNT(*) FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int
    
    // ========== Insert Operations ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Transaction
    suspend fun replaceAllSongs(songs: List<SongEntity>) {
        // Safety check: Don't wipe library with empty data
        if (songs.isEmpty()) {
            android.util.Log.w("SongDao", "Attempted to replace all songs with empty list - operation blocked to prevent data loss")
            return
        }
        deleteAllSongs()
        insertSongs(songs)
    }
    
    // ========== Update Operations ==========
    
    @Update
    suspend fun updateSong(song: SongEntity)
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: String, isFavorite: Boolean)
    
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE songs SET lastPlayed = :timestamp WHERE id = :songId")
    suspend fun updateLastPlayed(songId: String, timestamp: Long = System.currentTimeMillis())
    
    // ========== Delete Operations ==========
    
    @Delete
    suspend fun deleteSong(song: SongEntity)
    
    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    @Query("DELETE FROM songs WHERE id IN (:songIds)")
    suspend fun deleteSongs(songIds: List<String>)
}
