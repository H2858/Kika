package com.glassly.widgets

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.RemoteViews

class QuickWidget : BaseGlassWidget() {
    override val layoutRes: Int = R.layout.widget_quick
    override val defaultWidthDp: Int = 250
    override val defaultHeightDp: Int = 70

    override fun bind(context: Context, views: RemoteViews, style: GlassStyle, id: Int) {
        val actions = listOf(
            R.id.qs1 to Settings.ACTION_WIFI_SETTINGS,
            R.id.qs2 to Settings.ACTION_BLUETOOTH_SETTINGS,
            R.id.qs3 to Settings.ACTION_DISPLAY_SETTINGS,
            R.id.qs4 to Settings.ACTION_SOUND_SETTINGS,
            R.id.qs5 to Settings.ACTION_SETTINGS
        )
        actions.forEachIndexed { index, (viewId, action) ->
            views.setOnClickPendingIntent(
                viewId,
                activityPending(context, id * 10 + index, Intent(action))
            )
        }
    }
}
