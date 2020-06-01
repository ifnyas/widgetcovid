package app.ifnyas.widgetcovid.utils

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import app.ifnyas.widgetcovid.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class AppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId
            )
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val sm = SessionManager(context)
    val views = RemoteViews(
        context.packageName,
        R.layout.app_widget
    )

    // get val
    val time = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date())
    val place = sm.getPlace()!!.split(",")
    val title = place[0]
    var category = place[1]
    if (category == "Tinggi") {
        category = "Sangat Rawan"
        views.setTextColor(R.id.title_text, Color.RED)
        views.setImageViewResource(
            R.id.lens_img,
            R.drawable.ic_baseline_lens_red
        )
    }
    val titleText = "$title ($category)"

    views.setTextViewText(R.id.time_text, "Status hari ini ($time)")
    views.setTextViewText(R.id.title_text, titleText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}