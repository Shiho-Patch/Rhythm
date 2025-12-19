package chromahub.rhythm.app.cast

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.viewmodel.MusicViewModel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * HTTP server service that serves media files to Cast devices.
 * When casting to Chromecast, the device needs to fetch media via HTTP.
 */
class CastMediaServerService : Service() {

    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "CastMediaServerService"
        const val ACTION_START_SERVER = "ACTION_START_SERVER"
        const val ACTION_STOP_SERVER = "ACTION_STOP_SERVER"
        
        private val _isServerRunning = MutableStateFlow(false)
        val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()
        
        private val _serverAddress = MutableStateFlow<String?>(null)
        val serverAddress: StateFlow<String?> = _serverAddress.asStateFlow()
        
        private const val SERVER_PORT = 8080
        
        // Song lookup function - will be set by ViewModel
        var songLookup: ((String) -> chromahub.rhythm.app.data.Song?)? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVER -> startServer()
            ACTION_STOP_SERVER -> stopServer()
        }
        return START_NOT_STICKY
    }

    private fun startServer() {
        if (server != null) {
            Log.d(TAG, "Server already running")
            return
        }
        
        serviceScope.launch {
            try {
                val ipAddress = getDeviceIpAddress()
                if (ipAddress == null) {
                    Log.w(TAG, "No suitable IP address found; cannot start HTTP server")
                    stopSelf()
                    return@launch
                }
                
                _serverAddress.value = "http://$ipAddress:$SERVER_PORT"
                Log.d(TAG, "Starting Cast media server at ${_serverAddress.value}")

                server = embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0") {
                    routing {
                        // Serve song audio files
                        get("/song/{songId}") {
                            val songId = call.parameters["songId"]
                            if (songId == null) {
                                call.respond(HttpStatusCode.BadRequest, "Song ID is missing")
                                return@get
                            }

                            val song = songLookup?.invoke(songId)
                            if (song == null) {
                                call.respond(HttpStatusCode.NotFound, "Song not found")
                                return@get
                            }

                            try {
                                serveSongFile(call, song)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error serving song $songId", e)
                                call.respond(HttpStatusCode.InternalServerError, "Error serving file")
                            }
                        }
                        
                        // Serve album artwork
                        get("/art/{songId}") {
                            val songId = call.parameters["songId"]
                            if (songId == null) {
                                call.respond(HttpStatusCode.BadRequest, "Song ID is missing")
                                return@get
                            }

                            val song = songLookup?.invoke(songId)
                            if (song == null) {
                                call.respond(HttpStatusCode.NotFound, "Song not found")
                                return@get
                            }

                            try {
                                serveArtwork(call, song)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error serving artwork for $songId", e)
                                call.respond(HttpStatusCode.NotFound, "Artwork not available")
                            }
                        }
                        
                        // Health check endpoint
                        get("/health") {
                            call.respondText("OK")
                        }
                    }
                }.start(wait = false)
                
                _isServerRunning.value = true
                Log.d(TAG, "Cast media server started successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server", e)
                _isServerRunning.value = false
                _serverAddress.value = null
            }
        }
    }
    
    private suspend fun serveSongFile(call: ApplicationCall, song: chromahub.rhythm.app.data.Song) {
        val uri = song.uri
        
        contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val fileSize = pfd.statSize
            val rangeHeader = call.request.headers[HttpHeaders.Range]

            if (rangeHeader != null) {
                // Handle range request for seeking support
                val rangesSpecifier = io.ktor.http.parseRangesSpecifier(rangeHeader)
                val ranges = rangesSpecifier?.ranges

                if (ranges.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid range")
                    return
                }

                val range = ranges.first()
                val start = when (range) {
                    is io.ktor.http.ContentRange.Bounded -> range.from
                    is io.ktor.http.ContentRange.TailFrom -> range.from
                    is io.ktor.http.ContentRange.Suffix -> fileSize - range.lastCount
                    else -> 0L
                }
                val end = when (range) {
                    is io.ktor.http.ContentRange.Bounded -> range.to
                    is io.ktor.http.ContentRange.TailFrom -> fileSize - 1
                    is io.ktor.http.ContentRange.Suffix -> fileSize - 1
                    else -> fileSize - 1
                }

                val clampedStart = start.coerceAtLeast(0L)
                val clampedEnd = end.coerceAtMost(fileSize - 1)
                val length = clampedEnd - clampedStart + 1

                if (length <= 0) {
                    call.respond(HttpStatusCode.RequestedRangeNotSatisfiable, "Range not satisfiable")
                    return
                }

                val inputStream = java.io.FileInputStream(pfd.fileDescriptor)
                var skipped = 0L
                while (skipped < clampedStart) {
                    val s = inputStream.skip(clampedStart - skipped)
                    if (s <= 0) break
                    skipped += s
                }

                call.response.header(HttpHeaders.ContentRange, "bytes $clampedStart-$clampedEnd/$fileSize")
                call.response.header(HttpHeaders.AcceptRanges, "bytes")

                val bytes = withContext(Dispatchers.IO) {
                    inputStream.readNBytes(length.toInt())
                }

                call.respondBytes(bytes, ContentType.Audio.MPEG, HttpStatusCode.PartialContent)
            } else {
                // Full file request
                val inputStream = java.io.FileInputStream(pfd.fileDescriptor)
                call.response.header(HttpHeaders.AcceptRanges, "bytes")
                val bytes = withContext(Dispatchers.IO) {
                    inputStream.readBytes()
                }
                call.respondBytes(bytes, ContentType.Audio.MPEG)
            }
        } ?: run {
            call.respond(HttpStatusCode.NotFound, "File not found")
        }
    }
    
    private suspend fun serveArtwork(call: ApplicationCall, song: chromahub.rhythm.app.data.Song) {
        val artworkUri = song.artworkUri
        if (artworkUri == null) {
            call.respond(HttpStatusCode.NotFound, "No artwork")
            return
        }
        
        contentResolver.openInputStream(artworkUri)?.use { inputStream ->
            val bytes = withContext(Dispatchers.IO) {
                inputStream.readBytes()
            }
            call.respondBytes(bytes, ContentType.Image.JPEG)
        } ?: run {
            call.respond(HttpStatusCode.NotFound, "Artwork not found")
        }
    }

    private fun stopServer() {
        try {
            server?.stop(1000, 2000)
            server = null
            _isServerRunning.value = false
            _serverAddress.value = null
            Log.d(TAG, "Cast media server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopServer()
        serviceJob.cancel()
        super.onDestroy()
    }

    /**
     * Get the device's local IP address on the WiFi network
     */
    private fun getDeviceIpAddress(): String? {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return null
            
            // Only proceed if we're on WiFi (casting requires same network)
            if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.w(TAG, "Not on WiFi, Cast may not work")
            }
            
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return null
            
            for (linkAddress in linkProperties.linkAddresses) {
                val address = linkAddress.address
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    return address.hostAddress
                }
            }
            
            // Fallback: enumerate network interfaces
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (!networkInterface.isUp || networkInterface.isLoopback) continue
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
        }
        return null
    }
}
