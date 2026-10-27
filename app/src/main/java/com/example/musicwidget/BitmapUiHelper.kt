package com.example.musicwidget

import android.graphics.*
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

object BitmapUiHelper {

    private const val DEFAULT_TOP_LEFT_COLOR = 0xFF6B5344.toInt()
    private const val DEFAULT_BOTTOM_RIGHT_COLOR = 0xFF7D3C1D.toInt()

    fun createRoundedAlbumArt(source: Bitmap?, sizePx: Int = 240, cornerRadiusPx: Float = 48f): Bitmap {
        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())

        if (source != null) {
            val scaled = Bitmap.createScaledBitmap(source, sizePx, sizePx, true)
            val shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.shader = shader
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        } else {
            paint.color = 0xFFB8BABC.toInt()
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        }
        return output
    }

    fun createGradientCardBackground(
        albumArt: Bitmap?,
        widthPx: Int = 500,
        heightPx: Int = 500,
        cornerRadiusPx: Float = 110f
    ): Bitmap {
        var startColor = DEFAULT_TOP_LEFT_COLOR
        var endColor = DEFAULT_BOTTOM_RIGHT_COLOR

        if (albumArt != null) {
            val palette = Palette.from(albumArt).generate()
            val dominant = palette.getDominantColor(DEFAULT_TOP_LEFT_COLOR)
            val vibrant = palette.getDarkVibrantColor(
                palette.getMutedColor(DEFAULT_BOTTOM_RIGHT_COLOR)
            )
            startColor = ColorUtils.blendARGB(dominant, Color.parseColor("#5A4234"), 0.45f)
            endColor = ColorUtils.blendARGB(vibrant, Color.parseColor("#7A3918"), 0.45f)
        }

        val output = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val gradient = LinearGradient(
            0f, 0f,
            widthPx.toFloat(), heightPx.toFloat(),
            intArrayOf(startColor, endColor),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient

        val rect = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        return output
    }
}
