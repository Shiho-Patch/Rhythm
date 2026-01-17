package chromahub.rhythm.app.features.local.data.cache

import android.content.Context
import android.util.Log
import chromahub.rhythm.app.shared.data.model.Song
import chromahub.rhythm.app.shared.data.model.Album
import chromahub.rhythm.app.shared.data.model.Artist
import chromahub.rhythm.app.util.GsonUtils
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Persistent cache for music library to enable instant loading on app restart.
 * This prevents the "library vanishing" issue by preserving the song list between sessions.
 */
object LibraryCache {
    private const val TAG = "LibraryCache"
    private const val CACHE_DIR_NAME = "library_cache"
    private const val SONGS_CACHE_FILE = "songs_cache.json"
    private const val ALBUMS_CACHE_FILE = "albums_cache.json"
    private const val ARTISTS_CACHE_FILE = "artists_cache.json"
    private const val CACHE_VERSION_FILE = "cache_version.txt"
    private const val CURRENT_CACHE_VERSION = "2.0" // Semantic versioning: MAJOR.MINOR
    
    /**
     * Cache directory in app's files directory (not cleared by clearCacheOnExit)
     */
    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }
    
    /**
     * Saves the library data to persistent storage
     * Uses atomic writes with temp files to prevent corruption
     */
    suspend fun saveLibrary(
        context: Context,
        songs: List<Song>,
        albums: List<Album>,
        artists: List<Artist>
    ) = withContext(Dispatchers.IO) {
        try {
            val cacheDir = getCacheDir(context)
            
            // Write to temporary files first for atomic operation
            val versionTemp = File(cacheDir, "$CACHE_VERSION_FILE.tmp")
            val songsTemp = File(cacheDir, "$SONGS_CACHE_FILE.tmp")
            val albumsTemp = File(cacheDir, "$ALBUMS_CACHE_FILE.tmp")
            val artistsTemp = File(cacheDir, "$ARTISTS_CACHE_FILE.tmp")
            
            try {
                // Write all data to temp files
                versionTemp.writeText(CURRENT_CACHE_VERSION)
                songsTemp.writeText(GsonUtils.gson.toJson(songs))
                albumsTemp.writeText(GsonUtils.gson.toJson(albums))
                artistsTemp.writeText(GsonUtils.gson.toJson(artists))
                
                // Only if ALL writes succeeded, atomically rename to final files
                // This ensures we never have partial/corrupted cache
                val versionFinal = File(cacheDir, CACHE_VERSION_FILE)
                val songsFinal = File(cacheDir, SONGS_CACHE_FILE)
                val albumsFinal = File(cacheDir, ALBUMS_CACHE_FILE)
                val artistsFinal = File(cacheDir, ARTISTS_CACHE_FILE)
                
                versionTemp.renameTo(versionFinal)
                songsTemp.renameTo(songsFinal)
                albumsTemp.renameTo(albumsFinal)
                artistsTemp.renameTo(artistsFinal)
                
                Log.d(TAG, "Library cache saved atomically: ${songs.size} songs, ${albums.size} albums, ${artists.size} artists")
            } catch (e: Exception) {
                // Clean up temp files on any failure
                versionTemp.delete()
                songsTemp.delete()
                albumsTemp.delete()
                artistsTemp.delete()
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving library cache", e)
        }
    }
    
    /**
     * Loads the cached library data
     * @return Triple of (songs, albums, artists) or null if cache is invalid/missing
     * Implements partial recovery - returns available data even if some files are corrupted
     */
    suspend fun loadLibrary(context: Context): Triple<List<Song>, List<Album>, List<Artist>>? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = getCacheDir(context)
            
            // Check cache version with semantic versioning support
            val versionFile = File(cacheDir, CACHE_VERSION_FILE)
            if (!versionFile.exists()) {
                Log.d(TAG, "Cache version file missing, invalidating cache")
                return@withContext null
            }
            
            val cachedVersion = versionFile.readText()
            val isCompatible = try {
                val cachedMajor = cachedVersion.split(".").getOrNull(0)?.toIntOrNull() ?: 0
                val currentMajor = CURRENT_CACHE_VERSION.split(".").getOrNull(0)?.toIntOrNull() ?: 0
                cachedMajor == currentMajor // Only invalidate on major version change
            } catch (e: Exception) {
                false
            }
            
            if (!isCompatible) {
                Log.d(TAG, "Cache version incompatible (cached: $cachedVersion, current: $CURRENT_CACHE_VERSION), invalidating cache")
                return@withContext null
            }
            
            // Load each file with individual error handling for partial recovery
            val songs = try {
                val songsFile = File(cacheDir, SONGS_CACHE_FILE)
                if (!songsFile.exists()) {
                    Log.d(TAG, "Songs cache file not found")
                    return@withContext null
                }
                val songsJson = songsFile.readText()
                val songsType = object : TypeToken<List<Song>>() {}.type
                GsonUtils.gson.fromJson<List<Song>>(songsJson, songsType)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse songs cache", e)
                return@withContext null // Songs are critical, fail if corrupted
            }
            
            val albums = try {
                val albumsFile = File(cacheDir, ALBUMS_CACHE_FILE)
                if (!albumsFile.exists()) {
                    Log.w(TAG, "Albums cache file not found, using empty list")
                    emptyList<Album>()
                } else {
                    val albumsJson = albumsFile.readText()
                    val albumsType = object : TypeToken<List<Album>>() {}.type
                    GsonUtils.gson.fromJson<List<Album>>(albumsJson, albumsType)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse albums cache, using empty list", e)
                emptyList<Album>()
            }
            
            val artists = try {
                val artistsFile = File(cacheDir, ARTISTS_CACHE_FILE)
                if (!artistsFile.exists()) {
                    Log.w(TAG, "Artists cache file not found, using empty list")
                    emptyList<Artist>()
                } else {
                    val artistsJson = artistsFile.readText()
                    val artistsType = object : TypeToken<List<Artist>>() {}.type
                    GsonUtils.gson.fromJson<List<Artist>>(artistsJson, artistsType)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse artists cache, using empty list", e)
                emptyList<Artist>()
            }
            
            Log.d(TAG, "Library cache loaded: ${songs.size} songs, ${albums.size} albums, ${artists.size} artists")
            return@withContext Triple(songs, albums, artists)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading library cache", e)
            return@withContext null
        }
    }
    
    /**
     * Clears the library cache
     */
    fun clearCache(context: Context) {
        try {
            val cacheDir = getCacheDir(context)
            cacheDir.deleteRecursively()
            Log.d(TAG, "Library cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing library cache", e)
        }
    }
    
    /**
     * Checks if cache exists and is valid
     */
    fun isCacheValid(context: Context): Boolean {
        return try {
            val cacheDir = getCacheDir(context)
            val versionFile = File(cacheDir, CACHE_VERSION_FILE)
            val songsFile = File(cacheDir, SONGS_CACHE_FILE)
            
            if (!versionFile.exists() || !songsFile.exists()) {
                return false
            }
            
            // Check semantic version compatibility
            val cachedVersion = versionFile.readText()
            val cachedMajor = cachedVersion.split(".").getOrNull(0)?.toIntOrNull() ?: 0
            val currentMajor = CURRENT_CACHE_VERSION.split(".").getOrNull(0)?.toIntOrNull() ?: 0
            
            cachedMajor == currentMajor
        } catch (e: Exception) {
            false
        }
    }
}
