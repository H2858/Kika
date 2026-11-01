package com.glassly.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RemoteViews

/**
 * Shared logic: renders the glass background for the widget's real size,
 * then lets the concrete widget fill in its own content.
 */
abstract class BaseGlassWidget : AppWidgetProvider() {

    protected abstract val layoutRes: Int
    protected open val defaultWidthDp: Int = 180
    protected open val defaultHeightDp: Int = 110

    protected abstract fun bind(context: Context, views: RemoteViews, style: GlassStyle, id: Int)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { refresh(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        refresh(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { StyleStore.remove(context, it) }
    }

    private fun refresh(context: Context, manager: AppWidgetManager, id: Int) {
        val style = StyleStore.load(context, id)
        val views = RemoteViews(context.packageName, layoutRes)
        val (w, h) = sizeOf(context, manager, id)
        views.setImageViewBitmap(R.id.bg, GlassRenderer.render(context, style, w, h))
        bind(context, views, style, id)
        manager.updateAppWidget(id, views)
    }

    private fun sizeOf(context: Context, manager: AppWidgetManager, id: Int): Pair<Int, Int> {
        val o = manager.getAppWidgetOptions(id)
        val portrait = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        var w = o.getInt(
            if (portrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0
        )
        var h = o.getInt(
            if (portrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0
        )
        if (w <= 0) w = defaultWidthDp
        if (h <= 0) h = defaultHeightDp
        return w to h
    }

    protected fun activityPending(context: Context, requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    protected fun appIntent(context: Context): Intent = Intent(context, MainActivity::class.java)

    /** First intent that some installed app can handle, otherwise our own app. */
    protected fun firstResolvable(context: Context, vararg intents: Intent): Intent =
        intents.firstOrNull { it.resolveActivity(context.packageManager) != null } ?: appIntent(context)
}
