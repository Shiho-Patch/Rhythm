package chromahub.rhythm.app.features.local.data.db.dao

import androidx.room.*
import chromahub.rhythm.app.features.local.data.db.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Album operations
 */
@Dao
interface AlbumDao {
    
    // ========== Query Operations ==========
    
    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbumsFlow(): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums ORDER BY title ASC")
    suspend fun getAllAlbums(): List<AlbumEntity>
    
    @Query("SELECT * FROM albums WHERE id = :albumId LIMIT 1")
    suspend fun getAlbumById(albumId: String): AlbumEntity?
    
    @Query("SELECT * FROM albums WHERE artist = :artist ORDER BY year DESC, title ASC")
    suspend fun getAlbumsByArtist(artist: String): List<AlbumEntity>
    
    @Query("SELECT * FROM albums WHERE year = :year ORDER BY title ASC")
    suspend fun getAlbumsByYear(year: Int): List<AlbumEntity>
    
    @Query("SELECT * FROM albums ORDER BY year DESC LIMIT :limit")
    suspend fun getRecentAlbums(limit: Int = 20): List<AlbumEntity>
    
    @Query("""
        SELECT * FROM albums 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    suspend fun searchAlbums(query: String): List<AlbumEntity>
    
    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
    
    @Query("SELECT DISTINCT artist FROM albums ORDER BY artist ASC")
    suspend fun getAllArtistNames(): List<String>
    
    // ========== Insert Operations ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)
    
    @Transaction
    suspend fun replaceAllAlbums(albums: List<AlbumEntity>) {
        // Safety check: Don't wipe albums with empty data
        if (albums.isEmpty()) {
            android.util.Log.w("AlbumDao", "Attempted to replace all albums with empty list - operation blocked to prevent data loss")
            return
        }
        deleteAllAlbums()
        insertAlbums(albums)
    }
    
    // ========== Update Operations ==========
    
    @Update
    suspend fun updateAlbum(album: AlbumEntity)
    
    @Query("UPDATE albums SET artworkUriString = :artworkUri WHERE id = :albumId")
    suspend fun updateAlbumArtwork(albumId: String, artworkUri: String?)
    
    // ========== Delete Operations ==========
    
    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)
    
    @Query("DELETE FROM albums WHERE id = :albumId")
    suspend fun deleteAlbumById(albumId: String)
    
    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()
}
