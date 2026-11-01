package com.glassly.widgets

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews

class ClockWidget : BaseGlassWidget() {
    override val layoutRes: Int = R.layout.widget_clock

    override fun bind(context: Context, views: RemoteViews, style: GlassStyle, id: Int) {
        // 24h handled correctly: format is forced or follows the system setting.
        val (f12, f24) = when (style.hourMode) {
            1 -> "h:mm" to "h:mm"
            2 -> "HH:mm" to "HH:mm"
            else -> "h:mm" to "HH:mm"
        }
        views.setCharSequence(R.id.time, "setFormat12Hour", f12)
        views.setCharSequence(R.id.time, "setFormat24Hour", f24)
        views.setTextColor(R.id.time, style.textColor)
        views.setTextColor(R.id.date, style.textColor)

        val target = firstResolvable(context, Intent(AlarmClock.ACTION_SHOW_ALARMS))
        views.setOnClickPendingIntent(R.id.root, activityPending(context, id, target))
    }
}
