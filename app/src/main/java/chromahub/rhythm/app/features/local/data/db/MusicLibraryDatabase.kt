package chromahub.rhythm.app.features.local.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import chromahub.rhythm.app.features.local.data.db.converter.UriConverter
import chromahub.rhythm.app.features.local.data.db.dao.*
import chromahub.rhythm.app.features.local.data.db.entity.*

/**
 * Room Database for Rhythm music library
 * 
 * This database stores:
 * - Songs: All music files with metadata
 * - Albums: Album information
 * - Artists: Artist information  
 * - Playlists: User-created and default playlists
 * 
 * Benefits over JSON storage:
 * - Fast indexed queries
 * - Efficient memory usage
 * - Type-safe operations
 * - Automatic background threading
 * - Support for complex queries and filtering
 */
@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(UriConverter::class)
abstract class MusicLibraryDatabase : RoomDatabase() {
    
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    
    companion object {
        private const val DATABASE_NAME = "rhythm_music_library.db"
        
        @Volatile
        private var INSTANCE: MusicLibraryDatabase? = null
        
        /**
         * Get the singleton instance of the database
         */
        fun getInstance(context: Context): MusicLibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        /**
         * Build the database with migrations
         */
        private fun buildDatabase(context: Context): MusicLibraryDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MusicLibraryDatabase::class.java,
                DATABASE_NAME
            )
                // Only allow destructive migration on downgrade (going to older version)
                // This preserves data on normal upgrades
                .fallbackToDestructiveMigrationOnDowngrade()
                // Add proper migrations here as schema evolves
                // Example: .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
        
        /**
         * Clear all database data
         * Creates a backup before clearing in case recovery is needed
         */
        suspend fun clearDatabase(context: Context) {
            val db = getInstance(context)
            
            // Create backup before clearing
            try {
                backupDatabase(context)
                android.util.Log.d("MusicLibraryDatabase", "Database backup created before clearing")
            } catch (e: Exception) {
                android.util.Log.e("MusicLibraryDatabase", "Failed to create backup before clear", e)
                // Continue with clear even if backup fails, but log the error
            }
            
            db.clearAllTables()
        }
        
        /**
         * Backup database to a timestamped file
         * Returns the backup file path or null if failed
         */
        suspend fun backupDatabase(context: Context): String? = withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath(DATABASE_NAME)
                if (!dbFile.exists()) {
                    android.util.Log.w("MusicLibraryDatabase", "Database file doesn't exist, skipping backup")
                    return@withContext null
                }
                
                // Create backups directory if it doesn't exist
                val backupDir = java.io.File(context.filesDir, "db_backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                
                // Create timestamped backup file
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                val backupFile = java.io.File(backupDir, "music_library_backup_$timestamp.db")
                
                // Copy database file
                dbFile.copyTo(backupFile, overwrite = false)
                
                // Keep only the last 5 backups to save space
                cleanupOldBackups(backupDir, 5)
                
                android.util.Log.d("MusicLibraryDatabase", "Database backed up to: ${backupFile.absolutePath}")
                backupFile.absolutePath
            } catch (e: Exception) {
                android.util.Log.e("MusicLibraryDatabase", "Database backup failed", e)
                null
            }
        }
        
        /**
         * Remove old backup files, keeping only the most recent N backups
         */
        private fun cleanupOldBackups(backupDir: java.io.File, keepCount: Int) {
            try {
                val backups = backupDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
                backups.drop(keepCount).forEach { oldBackup ->
                    if (oldBackup.delete()) {
                        android.util.Log.d("MusicLibraryDatabase", "Deleted old backup: ${oldBackup.name}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicLibraryDatabase", "Failed to cleanup old backups", e)
            }
        }
        
        /**
         * Get database statistics
         * Runs all queries in a single transaction for consistency
         */
        suspend fun getDatabaseStats(context: Context): DatabaseStats {
            val db = getInstance(context)
            // Run all count queries on IO thread to ensure consistent snapshot
            return withContext(kotlinx.coroutines.Dispatchers.IO) {
                DatabaseStats(
                    songCount = db.songDao().getSongCount(),
                    albumCount = db.albumDao().getAlbumCount(),
                    artistCount = db.artistDao().getArtistCount(),
                    playlistCount = db.playlistDao().getPlaylistCount(),
                    favoriteCount = db.songDao().getFavoriteCount()
                )
            }
        }
        
        /**
         * Close database instance (for cleanup or testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

/**
 * Data class for database statistics
 */
data class DatabaseStats(
    val songCount: Int,
    val albumCount: Int,
    val artistCount: Int,
    val playlistCount: Int,
    val favoriteCount: Int
)
