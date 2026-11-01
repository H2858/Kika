package com.glassly.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.BatteryManager
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils

class BatteryWidget : BaseGlassWidget() {
    override val layoutRes: Int = R.layout.widget_battery

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        schedule(context)
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(alarmIntent(context))
    }

    override fun bind(context: Context, views: RemoteViews, style: GlassStyle, id: Int) {
        val bi = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bi?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bi?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale).coerceIn(0, 100) else 0
        val status = bi?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val ringColor = when {
            charging -> Color.parseColor("#5CE1FF")
            pct >= 50 -> Color.parseColor("#7CF29C")
            pct >= 20 -> Color.parseColor("#FFC857")
            else -> Color.parseColor("#FF6B6B")
        }
        views.setImageViewBitmap(R.id.ring, ring(pct, ringColor, ColorUtils.setAlphaComponent(style.textColor, 60)))
        views.setTextViewText(R.id.percent, "$pct%")
        views.setTextViewText(
            R.id.status,
            context.getString(if (charging) R.string.charging else R.string.discharging)
        )
        views.setTextColor(R.id.percent, style.textColor)
        views.setTextColor(R.id.title, style.textColor)
        views.setTextColor(R.id.status, style.textColor)

        val usage = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        views.setOnClickPendingIntent(R.id.root, activityPending(context, id, usage))
    }

    private fun ring(pct: Int, color: Int, track: Int): Bitmap {
        val size = 240
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val sw = 24f
        val oval = RectF(sw, sw, size - sw, size - sw)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = sw
            strokeCap = Paint.Cap.ROUND
        }
        p.color = track
        c.drawArc(oval, 0f, 360f, false, p)
        p.color = color
        if (pct > 0) c.drawArc(oval, -90f, 360f * pct / 100f, false, p)
        return bmp
    }

    private fun alarmIntent(context: Context): PendingIntent {
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, BatteryWidget::class.java))
        val i = Intent(context, BatteryWidget::class.java)
            .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        return PendingIntent.getBroadcast(
            context, 77, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /** Inexact, permission-free refresh roughly every 15 minutes. */
    private fun schedule(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 15 * 60_000L,
            alarmIntent(context)
        )
    }
}
