package com.glassly.widgets

import android.content.Context
import android.widget.RemoteViews
import java.util.Calendar

class QuoteWidget : BaseGlassWidget() {
    override val layoutRes: Int = R.layout.widget_quote

    override fun bind(context: Context, views: RemoteViews, style: GlassStyle, id: Int) {
        val quotes = context.resources.getStringArray(R.array.quotes)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val text = style.text.ifBlank { quotes[day % quotes.size] }
        views.setTextViewText(R.id.quote, text)
        views.setTextColor(R.id.quote, style.textColor)
        views.setOnClickPendingIntent(R.id.root, activityPending(context, id, appIntent(context)))
    }
}
