package chromahub.rhythm.app.features.local.data.db.converter

import android.net.Uri
import androidx.room.TypeConverter

/**
 * Type converter for Room to handle Uri objects
 */
class UriConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}
