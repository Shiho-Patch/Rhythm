@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package chromahub.rhythm.app.shared.presentation.components.common

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import chromahub.rhythm.app.shared.data.model.AppSettings

/**
 * Enum for shape targets that can be customized with expressive shapes
 */
enum class ExpressiveShapeTarget {
    ALBUM_ART,
    FAB,
    CARDS,
    BUTTONS,
    CHIPS,
    PLAYER_CONTROLS,
    MINI_PLAYER,
    NAV_INDICATOR
}

/**
 * Extension function to convert RoundedPolygon to a Compose Shape
 */
fun RoundedPolygon.toComposeShape(): Shape {
    return object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val path = this@toComposeShape.toPath().asComposePath()
            
            // The MaterialShapes are normalized to [-1, 1] so we need to scale them
            val matrix = Matrix()
            
            // Scale to fit the target size
            val scale = minOf(size.width, size.height) / 2f
            
            // Center the shape
            matrix.translate(size.width / 2f, size.height / 2f)
            matrix.scale(scale, scale)
            
            val scaledPath = Path()
            scaledPath.addPath(path)
            scaledPath.transform(matrix)
            
            return Outline.Generic(scaledPath)
        }
    }
}

/**
 * Get the MaterialShapes RoundedPolygon for a given shape ID
 */
private fun getShapeById(shapeId: String): RoundedPolygon? {
    return when (shapeId) {
        "CIRCLE" -> MaterialShapes.Circle
        "SQUARE" -> MaterialShapes.Square
        "OVAL" -> MaterialShapes.Oval
        "PILL" -> MaterialShapes.Pill
        "DIAMOND" -> MaterialShapes.Diamond
        "TRIANGLE" -> MaterialShapes.Triangle
        "PENTAGON" -> MaterialShapes.Pentagon
        "FLOWER" -> MaterialShapes.Flower
        "CLOVER_4_LEAF" -> MaterialShapes.Clover4Leaf
        "CLOVER_8_LEAF" -> MaterialShapes.Clover8Leaf
        "HEART" -> MaterialShapes.Heart
        "BOOM" -> MaterialShapes.Boom
        "SOFT_BOOM" -> MaterialShapes.SoftBoom
        "BURST" -> MaterialShapes.Burst
        "SOFT_BURST" -> MaterialShapes.SoftBurst
        "SUNNY" -> MaterialShapes.Sunny
        "VERY_SUNNY" -> MaterialShapes.VerySunny
        "COOKIE_4" -> MaterialShapes.Cookie4Sided
        "COOKIE_6" -> MaterialShapes.Cookie6Sided
        "COOKIE_7" -> MaterialShapes.Cookie7Sided
        "COOKIE_9" -> MaterialShapes.Cookie9Sided
        "COOKIE_12" -> MaterialShapes.Cookie12Sided
        "GHOSTISH" -> MaterialShapes.Ghostish
        "PUFFY" -> MaterialShapes.Puffy
        "PUFFY_DIAMOND" -> MaterialShapes.PuffyDiamond
        "BUN" -> MaterialShapes.Bun
        "FAN" -> MaterialShapes.Fan
        "ARROW" -> MaterialShapes.Arrow
        "ARCH" -> MaterialShapes.Arch
        "CLAM_SHELL" -> MaterialShapes.ClamShell
        "GEM" -> MaterialShapes.Gem
        "SEMI_CIRCLE" -> MaterialShapes.SemiCircle
        "SLANTED" -> MaterialShapes.Slanted
        "PIXEL_CIRCLE" -> MaterialShapes.PixelCircle
        "PIXEL_TRIANGLE" -> MaterialShapes.PixelTriangle
        else -> null
    }
}

/**
 * Get the default shape for a given target
 */
private fun getDefaultShapeForTarget(target: ExpressiveShapeTarget): Shape {
    return when (target) {
        ExpressiveShapeTarget.ALBUM_ART -> RoundedCornerShape(28.dp)
        ExpressiveShapeTarget.FAB -> CircleShape
        ExpressiveShapeTarget.CARDS -> RoundedCornerShape(20.dp)
        ExpressiveShapeTarget.BUTTONS -> CircleShape
        ExpressiveShapeTarget.CHIPS -> CircleShape
        ExpressiveShapeTarget.PLAYER_CONTROLS -> CircleShape
        ExpressiveShapeTarget.MINI_PLAYER -> RoundedCornerShape(16.dp)
        ExpressiveShapeTarget.NAV_INDICATOR -> CircleShape
    }
}

/**
 * Composable function to get the appropriate shape for a target.
 * Returns an expressive MaterialShape if enabled and configured, otherwise returns the default shape.
 * 
 * @param target The ExpressiveShapeTarget to get the shape for
 * @param fallbackShape Optional fallback shape if no expressive shape is configured
 * @return The configured Shape for the target
 */
@Composable
fun rememberExpressiveShapeFor(
    target: ExpressiveShapeTarget,
    fallbackShape: Shape? = null
): Shape {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    
    val expressiveShapesEnabled by appSettings.expressiveShapesEnabled.collectAsState()
    
    val shapeId = when (target) {
        ExpressiveShapeTarget.ALBUM_ART -> appSettings.expressiveShapeAlbumArt.collectAsState().value
        ExpressiveShapeTarget.FAB -> appSettings.expressiveShapeFab.collectAsState().value
        ExpressiveShapeTarget.CARDS -> appSettings.expressiveShapeCards.collectAsState().value
        ExpressiveShapeTarget.BUTTONS -> appSettings.expressiveShapeButtons.collectAsState().value
        ExpressiveShapeTarget.CHIPS -> appSettings.expressiveShapeChips.collectAsState().value
        ExpressiveShapeTarget.PLAYER_CONTROLS -> appSettings.expressiveShapePlayerControls.collectAsState().value
        ExpressiveShapeTarget.MINI_PLAYER -> appSettings.expressiveShapeMiniPlayer.collectAsState().value
        ExpressiveShapeTarget.NAV_INDICATOR -> appSettings.expressiveShapeNavIndicator.collectAsState().value
    }
    
    return remember(expressiveShapesEnabled, shapeId, fallbackShape) {
        if (!expressiveShapesEnabled) {
            fallbackShape ?: getDefaultShapeForTarget(target)
        } else {
            val roundedPolygon = getShapeById(shapeId)
            roundedPolygon?.toComposeShape() ?: fallbackShape ?: getDefaultShapeForTarget(target)
        }
    }
}

/**
 * Composable function to get expressive shape directly from a shape ID string
 * 
 * @param shapeId The string ID of the shape (e.g., "FLOWER", "HEART")
 * @param fallbackShape Fallback shape if the ID is not recognized
 * @return The configured Shape
 */
@Composable
fun rememberExpressiveShape(
    shapeId: String,
    fallbackShape: Shape = CircleShape
): Shape {
    return remember(shapeId) {
        val roundedPolygon = getShapeById(shapeId)
        roundedPolygon?.toComposeShape() ?: fallbackShape
    }
}

/**
 * Object containing static helpers for non-composable contexts
 */
object ExpressiveShapeProvider {
    /**
     * Get a Compose Shape from a MaterialShapes RoundedPolygon
     */
    fun getShape(roundedPolygon: RoundedPolygon): Shape {
        return roundedPolygon.toComposeShape()
    }
    
    /**
     * Get a Compose Shape from a shape ID
     */
    fun getShapeById(shapeId: String, fallback: Shape = CircleShape): Shape {
        val roundedPolygon = chromahub.rhythm.app.shared.presentation.components.common.getShapeById(shapeId)
        return roundedPolygon?.toComposeShape() ?: fallback
    }
    
    /**
     * Check if a given shape ID is valid
     */
    fun isValidShapeId(shapeId: String): Boolean {
        return chromahub.rhythm.app.shared.presentation.components.common.getShapeById(shapeId) != null
    }
    
    /**
     * Get all available shape IDs
     */
    fun getAllShapeIds(): List<String> {
        return listOf(
            "CIRCLE", "SQUARE", "OVAL", "PILL", "DIAMOND", "TRIANGLE", "PENTAGON",
            "FLOWER", "CLOVER_4_LEAF", "CLOVER_8_LEAF", "HEART",
            "BOOM", "SOFT_BOOM", "BURST", "SOFT_BURST", "SUNNY", "VERY_SUNNY",
            "COOKIE_4", "COOKIE_6", "COOKIE_7", "COOKIE_9", "COOKIE_12",
            "GHOSTISH", "PUFFY", "PUFFY_DIAMOND", "BUN", "FAN", "ARROW",
            "ARCH", "CLAM_SHELL", "GEM", "SEMI_CIRCLE", "SLANTED",
            "PIXEL_CIRCLE", "PIXEL_TRIANGLE"
        )
    }
}
