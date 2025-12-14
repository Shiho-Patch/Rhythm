package chromahub.rhythm.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.AudioEffect
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController

/**
 * Utility class for handling equalizer-related functionality
 */
object EqualizerUtils {
    private const val TAG = "EqualizerUtils"
    
    /**
     * Opens the system equalizer
     * 
     * @param context The context to use for starting the activity
     * @param audioSessionId Optional audio session ID (use 0 for system default)
     * @return true if the equalizer was opened successfully, false otherwise
     */
    fun openSystemEqualizer(context: Context, audioSessionId: Int = 0): Boolean {
        return try {
            // Try multiple approaches to open the system equalizer
            
            // Approach 1: Standard audio effect control panel with FLAG_ACTIVITY_NEW_TASK
            val standardIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (tryStartActivity(context, standardIntent)) {
                Log.d(TAG, "Opened system equalizer with standard intent")
                return true
            }
            
            // Approach 2: Try without audio session ID
            val noSessionIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (tryStartActivity(context, noSessionIntent)) {
                Log.d(TAG, "Opened system equalizer without session ID")
                return true
            }
            
            // Approach 3: Try specific equalizer apps
            if (tryOpenSpecificEqualizerApps(context)) {
                return true
            }
            
            // Approach 4: Try to open sound settings as fallback
            if (tryOpenSoundSettings(context)) {
                Toast.makeText(
                    context,
                    "Opening sound settings - look for equalizer option",
                    Toast.LENGTH_LONG
                ).show()
                return true
            }
            
            // Nothing worked
            Log.w(TAG, "No system equalizer app found")
            Toast.makeText(
                context, 
                "No system equalizer found. Use the built-in equalizer instead.", 
                Toast.LENGTH_LONG
            ).show()
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error opening system equalizer", e)
            Toast.makeText(
                context, 
                "Could not open equalizer: ${e.localizedMessage}", 
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    
    /**
     * Try to start an activity, returning true if successful
     */
    private fun tryStartActivity(context: Context, intent: Intent): Boolean {
        return try {
            val packageManager = context.packageManager
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent, 
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            
            if (resolveInfo.isNotEmpty()) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity", e)
            false
        }
    }
    
    /**
     * Try to open specific known equalizer apps
     */
    private fun tryOpenSpecificEqualizerApps(context: Context): Boolean {
        // List of known equalizer packages - expanded list
        val equalizerPackages = listOf(
            "com.android.musicfx",                    // Android's built-in equalizer
            "com.google.android.musicfx",             // Google's MusicFX
            "com.sec.android.app.soundalive",         // Samsung's equalizer
            "com.samsung.android.soundassistant",     // Samsung Sound Assistant
            "com.motorola.dtv.soundenhancer",         // Motorola's equalizer
            "com.motorola.audioeffects",              // Motorola Audio Effects
            "com.xiaomi.equalizer",                   // Xiaomi's equalizer
            "com.miui.audioeffect",                   // MIUI Audio Effect
            "com.oneplus.sound.tuner",                // OnePlus equalizer
            "com.oplus.audioeffect",                  // OPPO/OnePlus Audio Effect
            "com.sony.soundenhancement.spapp",        // Sony's equalizer
            "com.sonyericsson.music.audioeffect",     // Sony/Ericsson Audio Effect
            "com.google.android.soundpicker",         // Google's sound picker
            "com.huawei.audioeffectcenter",           // Huawei's equalizer
            "com.asus.visualmaster",                  // ASUS Visual Master (includes audio)
            "com.lge.equalizer",                      // LG Equalizer
            "com.htc.music.fx",                       // HTC Music FX
            "com.vivo.audiofx"                        // Vivo Audio FX
        )
        
        val packageManager = context.packageManager
        
        // Try each known equalizer package
        for (packageName in equalizerPackages) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Log.d(TAG, "Opened equalizer app: $packageName")
                    return true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Equalizer package not available: $packageName")
                // Continue trying other packages
            }
        }
        
        return false
    }
    
    /**
     * Try to open sound settings as a fallback
     */
    private fun tryOpenSoundSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened sound settings as fallback")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open sound settings", e)
            false
        }
    }
    
    /**
     * Check if system has an equalizer available
     */
    fun isSystemEqualizerAvailable(context: Context): Boolean {
        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        val packageManager = context.packageManager
        
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        
        return resolveInfo.isNotEmpty()
    }
} 