package chromahub.rhythm.app.cast

import android.net.Uri
import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import chromahub.rhythm.app.data.Song
import org.json.JSONObject

/**
 * Handles playback on Cast devices (Chromecast, Android TV, etc.)
 */
class CastPlayer(private val castSession: CastSession) {
    
    companion object {
        private const val TAG = "CastPlayer"
    }

    private val remoteMediaClient: RemoteMediaClient? = castSession.remoteMediaClient

    /**
     * Load a queue of songs to the cast device
     */
    fun loadQueue(
        songs: List<Song>,
        startIndex: Int,
        startPosition: Long,
        repeatMode: Int,
        serverAddress: String,
        autoPlay: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        val client = remoteMediaClient
        if (client == null) {
            Log.e(TAG, "RemoteMediaClient is null")
            onComplete(false)
            return
        }

        try {
            val mediaItems = songs.map { song ->
                song.toMediaQueueItem(serverAddress)
            }.toTypedArray()

            client.queueLoad(
                mediaItems,
                startIndex,
                repeatMode,
                startPosition,
                null
            ).setResultCallback { result ->
                if (result.status.isSuccess) {
                    if (autoPlay) {
                        client.play()
                    }

                    // Wait for remote to be ready before reporting success
                    waitForRemoteReady {
                        onComplete(true)
                    }
                } else {
                    Log.e(TAG, "Remote media client failed to load queue: ${result.status.statusMessage}")
                    onComplete(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading queue to cast device", e)
            onComplete(false)
        }
    }

    /**
     * Convert Song to MediaQueueItem for casting
     */
    private fun Song.toMediaQueueItem(serverAddress: String): MediaQueueItem {
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, this.title)
        mediaMetadata.putString(MediaMetadata.KEY_ARTIST, this.artist)
        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, this.album)
        
        // Add artwork URL
        val artUrl = "$serverAddress/art/${this.id}"
        mediaMetadata.addImage(WebImage(Uri.parse(artUrl)))

        // Build media URL
        val mediaUrl = "$serverAddress/song/${this.id}"
        val mediaInfo = MediaInfo.Builder(mediaUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("audio/mpeg")
            .setStreamDuration(this.duration)
            .setMetadata(mediaMetadata)
            .build()

        return MediaQueueItem.Builder(mediaInfo)
            .setCustomData(JSONObject().put("songId", this.id))
            .build()
    }

    /**
     * Wait for the remote media client to be ready
     */
    private fun waitForRemoteReady(onReady: () -> Unit) {
        val client = remoteMediaClient ?: return

        val callback = object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                val state = client.playerState
                val pos = client.approximateStreamPosition

                if (state == MediaStatus.PLAYER_STATE_PLAYING ||
                    state == MediaStatus.PLAYER_STATE_PAUSED ||
                    pos > 0
                ) {
                    try {
                        client.unregisterCallback(this)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to unregister temporary callback", e)
                    }
                    onReady()
                }
            }
        }

        try {
            client.registerCallback(callback)
            client.requestStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error waiting for remote ready", e)
            onReady() // Fallback to immediate complete if registration fails
        }
    }

    /**
     * Seek to a position in the current media
     */
    fun seek(position: Long) {
        val client = remoteMediaClient ?: return
        try {
            Log.d(TAG, "Seeking to position: $position ms")
            val seekOptions = MediaSeekOptions.Builder()
                .setPosition(position)
                .build()

            client.seek(seekOptions)
            client.requestStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking cast device", e)
        }
    }

    fun play() {
        remoteMediaClient?.play()
    }

    fun pause() {
        remoteMediaClient?.pause()
    }

    fun next() {
        remoteMediaClient?.queueNext(null)
    }

    fun previous() {
        remoteMediaClient?.queuePrev(null)
    }

    fun jumpToItem(itemId: Int, position: Long) {
        remoteMediaClient?.queueJumpToItem(itemId, position, null)
    }

    fun setRepeatMode(repeatMode: Int) {
        remoteMediaClient?.queueSetRepeatMode(repeatMode, null)
    }
    
    fun stop() {
        remoteMediaClient?.stop()
    }
    
    fun getPlayerState(): Int {
        return remoteMediaClient?.playerState ?: MediaStatus.PLAYER_STATE_UNKNOWN
    }
    
    fun getCurrentPosition(): Long {
        return remoteMediaClient?.approximateStreamPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return remoteMediaClient?.streamDuration ?: 0L
    }
}
