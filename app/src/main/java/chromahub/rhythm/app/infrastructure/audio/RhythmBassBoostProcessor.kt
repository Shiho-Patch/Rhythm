package chromahub.rhythm.app.infrastructure.audio

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max
import kotlin.math.PI

/**
 * Rhythm Bass Boost Processor - Real-time bass enhancement
 * 
 * Uses a high-quality IIR low-pass filter with variable gain to enhance low frequencies.
 * Provides natural bass enhancement without file I/O latency, optimized for the Rhythm player.
 * 
 * Algorithm: Single-pole IIR low-pass filter + adaptive gain
 * Cutoff frequency: 150Hz
 * Gain range: 1.0x to 4.0x based on strength (0-1000)
 * Latency: <1ms per buffer
 */
@OptIn(UnstableApi::class)
class RhythmBassBoostProcessor : RhythmAudioProcessor() {
    
    companion object {
        private const val TAG = "RhythmBassBoost"
        private const val BASS_CUTOFF_FREQ = 150.0 // Hz - Optimized for music playback
    }
    
    // Bass boost strength (0-1000, where 1000 = maximum boost)
    private var strength: Short = 0
    private var enabled: Boolean = false
    
    // Filter state (per channel) - maintains continuity across buffers
    private var prevSample = FloatArray(2) // Support stereo
    private var filterCoeff = 0f
    
    /**
     * Enable or disable bass boost
     */
    fun setEnabled(enable: Boolean) {
        Log.d(TAG, "Bass boost enabled: $enable")
        this.enabled = enable
        if (!enable) {
            // Reset filter state to avoid artifacts
            prevSample.fill(0f)
        }
    }
    
    /**
     * Set bass boost strength
     * @param strength Strength value from 0 to 1000
     */
    fun setStrength(strength: Short) {
        this.strength = strength.coerceIn(0, 1000)
        updateFilterCoeff()
        Log.d(TAG, "Bass boost strength set to: ${this.strength}")
    }
    
    /**
     * Get current strength
     */
    fun getStrength(): Short = strength
    
    override fun isEnabled(): Boolean = enabled
    
    /**
     * Update filter coefficient based on current sample rate
     */
    private fun updateFilterCoeff() {
        if (sampleRate > 0) {
            val rc = 1.0 / (2.0 * PI * BASS_CUTOFF_FREQ)
            val dt = 1.0 / sampleRate
            filterCoeff = (dt / (rc + dt)).toFloat()
        }
    }
    
    override fun processSamples(samples: ShortArray) {
        if (!enabled || strength == 0.toShort()) {
            return // Pass through unchanged for maximum efficiency
        }
        
        // Update filter coefficient if not set
        if (filterCoeff == 0f) {
            updateFilterCoeff()
        }
        
        // Convert strength (0-1000) to linear gain (1.0-4.0)
        // Using a logarithmic curve for more natural and musical response
        val gain = when {
            strength == 0.toShort() -> 1.0f
            strength <= 100 -> 1.0f + (strength / 100.0f) * 0.3f  // 0-100 = 1.0-1.3x (subtle)
            strength <= 500 -> 1.3f + ((strength - 100) / 400.0f) * 0.9f  // 100-500 = 1.3-2.2x (medium)
            else -> 2.2f + ((strength - 500) / 500.0f) * 1.8f  // 500-1000 = 2.2-4.0x (strong)
        }
        
        val isStereo = channelCount == 2
        
        // Process each sample with the IIR filter
        for (i in samples.indices) {
            val channelIdx = if (isStereo) i % 2 else 0
            
            // Convert to normalized float (-1.0 to 1.0)
            val input = samples[i] / 32768.0f
            
            // Apply low-pass IIR filter to extract bass frequencies
            val lowPass = prevSample[channelIdx] + filterCoeff * (input - prevSample[channelIdx])
            prevSample[channelIdx] = lowPass
            
            // Mix original signal with amplified bass
            val bassBoost = lowPass * (gain - 1.0f)
            val output = input + bassBoost
            
            // Clamp to prevent clipping and convert back to 16-bit PCM
            samples[i] = (max(-1.0f, min(1.0f, output)) * 32767.0f).toInt().toShort()
        }
    }
}
