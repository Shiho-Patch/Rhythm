package chromahub.rhythm.app.infrastructure.audio

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Rhythm Audio Processor - Base class for real-time audio effects.
 * 
 * Processes audio samples in-place using custom DSP algorithms for zero-latency playback.
 * Part of the Rhythm music player's advanced audio processing pipeline.
 */
@OptIn(UnstableApi::class)
abstract class RhythmAudioProcessor : AudioProcessor {
    
    companion object {
        private const val TAG = "RhythmAudioProcessor"
    }
    
    private var inputAudioFormat: AudioProcessor.AudioFormat = AudioProcessor.AudioFormat.NOT_SET
    private var outputAudioFormat: AudioProcessor.AudioFormat = AudioProcessor.AudioFormat.NOT_SET
    private var inputEnded = false
    private var outputBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    
    // Audio format parameters
    protected var sampleRate: Int = 44100
    protected var channelCount: Int = 2
    protected var encoding: Int = C.ENCODING_PCM_16BIT
    
    /**
     * Process audio samples in-place using custom DSP algorithm
     * @param samples Array of audio samples (16-bit PCM)
     */
    abstract fun processSamples(samples: ShortArray)
    
    /**
     * Check if the processor is enabled
     */
    abstract fun isEnabled(): Boolean
    
    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        Log.d(TAG, "configure() - sampleRate=${inputAudioFormat.sampleRate}, channels=${inputAudioFormat.channelCount}, encoding=${inputAudioFormat.encoding}")
        
        this.inputAudioFormat = inputAudioFormat
        this.sampleRate = inputAudioFormat.sampleRate
        this.channelCount = inputAudioFormat.channelCount
        this.encoding = inputAudioFormat.encoding
        this.outputAudioFormat = inputAudioFormat
        
        return outputAudioFormat
    }
    
    override fun isActive(): Boolean {
        val active = isEnabled() && 
            inputAudioFormat != AudioProcessor.AudioFormat.NOT_SET &&
            encoding == C.ENCODING_PCM_16BIT
        return active
    }
    
    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!isActive()) {
            // Pass through unchanged
            outputBuffer = inputBuffer
            return
        }
        
        // Process audio samples in-place
        val position = inputBuffer.position()
        val samples = ShortArray(inputBuffer.remaining() / 2)
        inputBuffer.asShortBuffer().get(samples)
        
        // Apply audio processing
        processSamples(samples)
        
        // Create output buffer
        val output = ByteBuffer.allocateDirect(samples.size * 2).order(ByteOrder.nativeOrder())
        output.asShortBuffer().put(samples)
        output.limit(samples.size * 2)
        outputBuffer = output
        
        inputBuffer.position(position + samples.size * 2)
    }
    
    override fun getOutput(): ByteBuffer {
        val buffer = outputBuffer
        outputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
        return buffer
    }
    
    override fun queueEndOfStream() {
        Log.d(TAG, "queueEndOfStream()")
        inputEnded = true
    }
    
    override fun isEnded(): Boolean {
        return inputEnded && outputBuffer.remaining() == 0
    }
    
    override fun flush() {
        Log.d(TAG, "flush()")
        outputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
        inputEnded = false
    }
    
    override fun reset() {
        Log.d(TAG, "reset() called - preserving audio format configuration")
        flush()
        // Don't clear the audio format - Media3 may not call configure() again
        // and we want the processor to remain active if it was configured
    }
}
