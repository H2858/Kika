package com.glassly.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** All user-adjustable look settings of one widget instance. */
data class GlassStyle(
    val tint: Int = Color.WHITE,
    val opacity: Float = 0.25f,
    val radiusDp: Float = 28f,
    val border: Boolean = true,
    val textColor: Int = Color.WHITE,
    /** 0 = follow system, 1 = force 12h, 2 = force 24h */
    val hourMode: Int = 0,
    val text: String = ""
)

/** Persists a [GlassStyle] per appWidgetId. */
object StyleStore {
    private const val FILE = "glass_styles"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(context: Context, id: Int): GlassStyle {
        val d = GlassStyle()
        val p = prefs(context)
        return GlassStyle(
            tint = p.getInt("$id.tint", d.tint),
            opacity = p.getFloat("$id.opacity", d.opacity),
            radiusDp = p.getFloat("$id.radius", d.radiusDp),
            border = p.getBoolean("$id.border", d.border),
            textColor = p.getInt("$id.text_color", d.textColor),
            hourMode = p.getInt("$id.hour_mode", d.hourMode),
            text = p.getString("$id.text", d.text) ?: ""
        )
    }

    fun save(context: Context, id: Int, s: GlassStyle) {
        prefs(context).edit()
            .putInt("$id.tint", s.tint)
            .putFloat("$id.opacity", s.opacity)
            .putFloat("$id.radius", s.radiusDp)
            .putBoolean("$id.border", s.border)
            .putInt("$id.text_color", s.textColor)
            .putInt("$id.hour_mode", s.hourMode)
            .putString("$id.text", s.text)
            .apply()
    }

    fun remove(context: Context, id: Int) {
        val p = prefs(context)
        val e = p.edit()
        p.all.keys.filter { it.startsWith("$id.") }.forEach { e.remove(it) }
        e.apply()
    }
}

/** Draws the glass panel (translucent fill + highlight + shade + gradient border) into a bitmap. */
object GlassRenderer {
    private const val MAX_PIXELS = 450_000f

    fun render(context: Context, style: GlassStyle, widthDp: Int, heightDp: Int): Bitmap {
        var scale = min(context.resources.displayMetrics.density, 2.5f)
        val pixels = widthDp * scale * heightDp * scale
        if (pixels > MAX_PIXELS) scale *= sqrt(MAX_PIXELS / pixels)

        val w = max(2, (widthDp * scale).roundToInt())
        val h = max(2, (heightDp * scale).roundToInt())
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val stroke = 1.3f * scale
        val rect = RectF(stroke, stroke, w - stroke, h - stroke)
        val r = min(style.radiusDp * scale, min(rect.width(), rect.height()) / 2f)

        // 1) translucent tinted fill
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColorUtils.setAlphaComponent(
                style.tint,
                (style.opacity.coerceIn(0f, 1f) * 255f).roundToInt()
            )
        }
        canvas.drawRoundRect(rect, r, r, fill)

        // 2) soft diagonal light reflection
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(110, 255, 255, 255),
                    Color.argb(18, 255, 255, 255),
                    Color.argb(0, 255, 255, 255)
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, r, r, highlight)

        // 3) subtle bottom shade for depth
        val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, rect.top, 0f, rect.bottom,
                intArrayOf(Color.TRANSPARENT, Color.argb(34, 0, 0, 0)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, r, r, shade)

        // 4) glass edge
        if (style.border) {
            val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                strokeWidth = stroke
                shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    intArrayOf(
                        Color.argb(210, 255, 255, 255),
                        Color.argb(30, 255, 255, 255),
                        Color.argb(120, 255, 255, 255)
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(rect, r, r, edge)
        }
        return bmp
    }
}
