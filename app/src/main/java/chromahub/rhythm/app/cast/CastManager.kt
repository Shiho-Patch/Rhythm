package chromahub.rhythm.app.cast

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import chromahub.rhythm.app.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Google Cast sessions and controls Cast playback
 */
class CastManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CastManager"
        
        @Volatile
        private var INSTANCE: CastManager? = null
        
        fun getInstance(context: Context): CastManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CastManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var castContext: CastContext? = null
    private var sessionManager: SessionManager? = null
    private var castSession: CastSession? = null
    private var castPlayer: CastPlayer? = null
    private var localPlayer: androidx.media3.common.Player? = null
    
    // State flows
    private val _isCastAvailable = MutableStateFlow(false)
    val isCastAvailable: StateFlow<Boolean> = _isCastAvailable.asStateFlow()
    
    private val _isCasting = MutableStateFlow(false)
    val isCasting: StateFlow<Boolean> = _isCasting.asStateFlow()
    
    private val _castDeviceName = MutableStateFlow<String?>(null)
    val castDeviceName: StateFlow<String?> = _castDeviceName.asStateFlow()
    
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()
    
    // Callback for playback state changes
    var onCastPlaybackStateChanged: ((isPlaying: Boolean, position: Long) -> Unit)? = null
    
    private val castStateListener = CastStateListener { state ->
        Log.d(TAG, "Cast state changed: $state")
        _isCastAvailable.value = state != CastState.NO_DEVICES_AVAILABLE
    }
    
    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) {
            Log.d(TAG, "Cast session starting")
            _isConnecting.value = true
        }
        
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            Log.d(TAG, "Cast session started: $sessionId")
            castSession = session
            castPlayer = CastPlayer(session)
            _isCasting.value = true
            _isConnecting.value = false
            _castDeviceName.value = session.castDevice?.friendlyName
            
            // Start the media server
            startMediaServer()
            
            // Set up remote media client callbacks
            setupRemoteMediaCallbacks()
            
            // Transfer playback from local to cast
            transferToCast()
        }
        
        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Log.e(TAG, "Cast session start failed: $error")
            _isConnecting.value = false
            _isCasting.value = false
        }
        
        override fun onSessionEnding(session: CastSession) {
            Log.d(TAG, "Cast session ending")
        }
        
        override fun onSessionEnded(session: CastSession, error: Int) {
            Log.d(TAG, "Cast session ended: $error")
            
            // Transfer playback back to local before cleaning up
            transferFromCast()
            
            castSession = null
            castPlayer = null
            _isCasting.value = false
            _castDeviceName.value = null
            
            // Stop the media server
            stopMediaServer()
        }
        
        override fun onSessionResuming(session: CastSession, sessionId: String) {
            Log.d(TAG, "Cast session resuming: $sessionId")
            _isConnecting.value = true
        }
        
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            Log.d(TAG, "Cast session resumed, wasSuspended: $wasSuspended")
            castSession = session
            castPlayer = CastPlayer(session)
            _isCasting.value = true
            _isConnecting.value = false
            _castDeviceName.value = session.castDevice?.friendlyName
            
            startMediaServer()
            setupRemoteMediaCallbacks()
            
            // Transfer playback to cast if resuming
            if (!wasSuspended) {
                transferToCast()
            }
        }
        
        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            Log.e(TAG, "Cast session resume failed: $error")
            _isConnecting.value = false
            _isCasting.value = false
        }
        
        override fun onSessionSuspended(session: CastSession, reason: Int) {
            Log.d(TAG, "Cast session suspended: $reason")
        }
    }
    
    /**
     * Initialize the Cast manager
     */
    fun initialize() {
        try {
            castContext = CastContext.getSharedInstance(context)
            sessionManager = castContext?.sessionManager
            
            castContext?.addCastStateListener(castStateListener)
            sessionManager?.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
            
            // Check if there's an existing session
            val currentSession = sessionManager?.currentCastSession
            if (currentSession != null && currentSession.isConnected) {
                castSession = currentSession
                castPlayer = CastPlayer(currentSession)
                _isCasting.value = true
                _castDeviceName.value = currentSession.castDevice?.friendlyName
                startMediaServer()
                setupRemoteMediaCallbacks()
            }
            
            Log.d(TAG, "CastManager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CastManager", e)
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        try {
            castContext?.removeCastStateListener(castStateListener)
            sessionManager?.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
            stopMediaServer()
            Log.d(TAG, "CastManager released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing CastManager", e)
        }
    }
    
    /**
     * Play a queue of songs on the Cast device
     */
    fun playQueue(
        songs: List<Song>,
        startIndex: Int = 0,
        startPosition: Long = 0L,
        autoPlay: Boolean = true,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val serverAddress = CastMediaServerService.serverAddress.value
        if (serverAddress == null) {
            Log.e(TAG, "Media server not running")
            onComplete(false)
            return
        }
        
        castPlayer?.loadQueue(
            songs = songs,
            startIndex = startIndex,
            startPosition = startPosition,
            repeatMode = MediaStatus.REPEAT_MODE_REPEAT_OFF,
            serverAddress = serverAddress,
            autoPlay = autoPlay,
            onComplete = onComplete
        )
    }
    
    /**
     * Play/pause on Cast device
     */
    fun playPause() {
        castPlayer?.let { player ->
            if (player.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING) {
                player.pause()
            } else {
                player.play()
            }
        }
    }
    
    /**
     * Skip to next track
     */
    fun next() {
        castPlayer?.next()
    }
    
    /**
     * Skip to previous track
     */
    fun previous() {
        castPlayer?.previous()
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        castPlayer?.seek(position)
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        castPlayer?.stop()
    }
    
    /**
     * End the Cast session
     */
    fun endSession() {
        sessionManager?.endCurrentSession(true)
    }
    
    /**
     * Set the local player reference for state transfer
     */
    fun setLocalPlayer(player: androidx.media3.common.Player) {
        this.localPlayer = player
    }
    
    /**
     * Transfer playback from local player to Cast device
     */
    fun transferToCast() {
        val local = localPlayer ?: return
        val cast = castPlayer ?: return
        
        try {
            // Save current state from local player
            val currentPosition = local.currentPosition
            val isPlaying = local.isPlaying
            val currentMediaItem = local.currentMediaItem
            
            // For now, just log the transfer - full implementation would require
            // CastPlayer to implement Player interface or manual state sync
            Log.d(TAG, "Transferring playback to Cast - Position: $currentPosition, Playing: $isPlaying")
            
            // TODO: Implement actual state transfer when CastPlayer supports Player interface
            // PlayerStateTransfer.transferPlayback(local, cast)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error transferring to Cast", e)
        }
    }
    
    /**
     * Transfer playback from Cast device back to local player
     */
    fun transferFromCast() {
        val cast = castPlayer ?: return
        val local = localPlayer ?: return
        
        try {
            // Get current state from cast player
            val currentPosition = getCurrentPosition()
            val isPlaying = isPlaying()
            
            // Resume on local player at the same position
            local.seekTo(currentPosition)
            if (isPlaying) {
                local.play()
            } else {
                local.pause()
            }
            
            Log.d(TAG, "Transferred playback from Cast - Position: $currentPosition, Playing: $isPlaying")
            
            // TODO: Implement actual state transfer when CastPlayer supports Player interface
            // PlayerStateTransfer.transferPlayback(cast, local)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error transferring from Cast", e)
        }
    }
    
    /**
     * Get the current Cast player state
     */
    fun isPlaying(): Boolean {
        return castPlayer?.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING
    }
    
    /**
     * Get current playback position
     */
    fun getCurrentPosition(): Long {
        return castPlayer?.getCurrentPosition() ?: 0L
    }
    
    /**
     * Get current track duration
     */
    fun getDuration(): Long {
        return castPlayer?.getDuration() ?: 0L
    }
    
    private fun setupRemoteMediaCallbacks() {
        castSession?.remoteMediaClient?.registerCallback(object : com.google.android.gms.cast.framework.media.RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                val isPlaying = castSession?.remoteMediaClient?.isPlaying ?: false
                val position = castSession?.remoteMediaClient?.approximateStreamPosition ?: 0L
                onCastPlaybackStateChanged?.invoke(isPlaying, position)
            }
        })
    }
    
    private fun startMediaServer() {
        val intent = Intent(context, CastMediaServerService::class.java).apply {
            action = CastMediaServerService.ACTION_START_SERVER
        }
        context.startService(intent)
    }
    
    private fun stopMediaServer() {
        val intent = Intent(context, CastMediaServerService::class.java).apply {
            action = CastMediaServerService.ACTION_STOP_SERVER
        }
        context.startService(intent)
    }
}
