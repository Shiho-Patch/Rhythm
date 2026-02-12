package chromahub.rhythm.app.infrastructure.audio

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import java.util.ArrayList

/**
 * Custom RenderersFactory that creates audio renderers configured for bit-perfect playback.
 * 
 * This factory creates MediaCodecAudioRenderer instances that use a custom AudioSink
 * capable of outputting audio at its native sample rate without resampling.
 */
@OptIn(UnstableApi::class)
class BitPerfectRenderersFactory(
    context: Context,
    private val enableBitPerfect: Boolean = false
) : DefaultRenderersFactory(context) {
    
    companion object {
        private const val TAG = "BitPerfectFactory"
    }
    
    init {
        Log.d(TAG, "Creating BitPerfectRenderersFactory (bit-perfect: $enableBitPerfect)")
        
        // Prefer extension renderers (like FFmpeg) if available for better format support
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
        
        if (enableBitPerfect) {
            Log.i(TAG, "Bit-perfect mode enabled - audio will output at native sample rate")
        }
    }
    
    /**
     * Override buildAudioRenderers to inject our custom AudioSink
     */
    override fun buildAudioRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        audioSink: AudioSink,
        eventHandler: Handler,
        eventListener: AudioRendererEventListener,
        out: ArrayList<Renderer>
    ) {
        Log.d(TAG, "Building audio renderers for bit-perfect playback")
        
        // Create our custom audio sink if bit-perfect is enabled
        val customAudioSink = if (enableBitPerfect) {
            Log.d(TAG, "Using BitPerfectAudioSink")
            BitPerfectAudioSink.create(context, enableBitPerfect)
        } else {
            Log.d(TAG, "Using standard AudioSink")
            audioSink
        }
        
        // Add the MediaCodec audio renderer with our custom sink
        val audioRenderer = MediaCodecAudioRenderer(
            context,
            mediaCodecSelector,
            enableDecoderFallback,
            eventHandler,
            eventListener,
            customAudioSink
        )
        
        out.add(audioRenderer)
        Log.d(TAG, "Audio renderer configured: bit-perfect=$enableBitPerfect")
        
        // Let the parent class add extension renderers if available
        if (extensionRendererMode != EXTENSION_RENDERER_MODE_OFF) {
            val extensionRendererIndex = out.size
            super.buildAudioRenderers(
                context,
                extensionRendererMode,
                mediaCodecSelector,
                enableDecoderFallback,
                customAudioSink,
                eventHandler,
                eventListener,
                out
            )
            
            // Log if extension renderers were added
            if (out.size > extensionRendererIndex + 1) {
                Log.d(TAG, "Extension audio renderers added: ${out.size - extensionRendererIndex - 1}")
            }
        }
    }
}
