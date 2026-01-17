package chromahub.rhythm.app.features.local.data.db.dao

import androidx.room.*
import chromahub.rhythm.app.features.local.data.db.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Artist operations
 */
@Dao
interface ArtistDao {
    
    // ========== Query Operations ==========
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtistsFlow(): Flow<List<ArtistEntity>>
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getAllArtists(): List<ArtistEntity>
    
    @Query("SELECT * FROM artists WHERE id = :artistId LIMIT 1")
    suspend fun getArtistById(artistId: String): ArtistEntity?
    
    @Query("SELECT * FROM artists WHERE name = :name LIMIT 1")
    suspend fun getArtistByName(name: String): ArtistEntity?
    
    @Query("""
        SELECT * FROM artists 
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun searchArtists(query: String): List<ArtistEntity>
    
    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
    
    @Query("SELECT * FROM artists ORDER BY numberOfTracks DESC LIMIT :limit")
    suspend fun getTopArtists(limit: Int = 20): List<ArtistEntity>
    
    // ========== Insert Operations ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)
    
    @Transaction
    suspend fun replaceAllArtists(artists: List<ArtistEntity>) {
        // Safety check: Don't wipe artists with empty data
        if (artists.isEmpty()) {
            android.util.Log.w("ArtistDao", "Attempted to replace all artists with empty list - operation blocked to prevent data loss")
            return
        }
        deleteAllArtists()
        insertArtists(artists)
    }
    
    // ========== Update Operations ==========
    
    @Update
    suspend fun updateArtist(artist: ArtistEntity)
    
    @Query("UPDATE artists SET artworkUriString = :artworkUri WHERE id = :artistId")
    suspend fun updateArtistArtwork(artistId: String, artworkUri: String?)
    
    // ========== Delete Operations ==========
    
    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)
    
    @Query("DELETE FROM artists WHERE id = :artistId")
    suspend fun deleteArtistById(artistId: String)
    
    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()
}
