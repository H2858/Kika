package com.glassly.widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.RemoteViews

class DateWidget : BaseGlassWidget() {
    override val layoutRes: Int = R.layout.widget_date
    override val defaultWidthDp: Int = 110
    override val defaultHeightDp: Int = 110

    override fun bind(context: Context, views: RemoteViews, style: GlassStyle, id: Int) {
        views.setTextColor(R.id.month, style.textColor)
        views.setTextColor(R.id.day, style.textColor)
        views.setTextColor(R.id.weekday, style.textColor)

        val uri = ContentUris.appendId(
            CalendarContract.CONTENT_URI.buildUpon().appendPath("time"),
            System.currentTimeMillis()
        ).build()
        val target = firstResolvable(context, Intent(Intent.ACTION_VIEW, uri))
        views.setOnClickPendingIntent(R.id.root, activityPending(context, id, target))
    }
}
