package com.example.digitidentifier
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset

object ImagePreprocessor {
    fun toMnistInput(
        strokes: List<List<Offset>>,
        widthPx: Int,
        heightPx: Int,
    ): FloatArray {
        val size = 280
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(AndroidColor.BLACK)

        val paint = Paint().apply {
            color = AndroidColor.WHITE
            strokeWidth = 28f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val sx = size / widthPx.toFloat()
        val sy = size / heightPx.toFloat()
        strokes.forEach { stroke ->
            for (i in 1 until stroke.size) {
                canvas.drawLine(
                    stroke[i - 1].x * sx, stroke[i - 1].y * sy,
                    stroke[i].x * sx, stroke[i].y * sy,
                    paint,
                )
            }
        }

        // Find the bounding box of white pixels
        var minX = size; var minY = size; var maxX = -1; var maxY = -1
        for (y in 0 until size) for (x in 0 until size) {
            if ((bitmap.getPixel(x, y) and 0xFF) > 0) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
        if (maxX < 0) return FloatArray(28 * 28)  // nothing drawn

// Crop, then scale the longer side to 20px and center in 28x28
        val cropped = Bitmap.createBitmap(bitmap, minX, minY, maxX - minX + 1, maxY - minY + 1)
        val scale = 20f / maxOf(cropped.width, cropped.height)
        val w = (cropped.width * scale).toInt().coerceAtLeast(1)
        val h = (cropped.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(cropped, w, h, true)

        val out = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888)
        AndroidCanvas(out).apply {
            drawColor(AndroidColor.BLACK)
            drawBitmap(scaled, (28 - w) / 2f, (28 - h) / 2f, null)
        }
        val pixels = IntArray(28 * 28)
        out.getPixels(pixels, 0, 28, 0, 0, 28, 28)
        return FloatArray(28 * 28) { (pixels[it] and 0xFF) / 255f }
        //v1-simple
//        val small = Bitmap.createScaledBitmap(bitmap, 28, 28, true)
//        val pixels = IntArray(28 * 28)
//        small.getPixels(pixels, 0, 28, 0, 0, 28, 28)
//        return FloatArray(28 * 28) { (pixels[it] and 0xFF) / 255f }
    }
}