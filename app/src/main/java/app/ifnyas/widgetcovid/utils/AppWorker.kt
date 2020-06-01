package app.ifnyas.widgetcovid.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters


class AppWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Do the work here
        updateWidget()

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun updateWidget() {
        val intent = Intent(applicationContext, AppWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(
            ComponentName(applicationContext, AppWidget::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        applicationContext.sendBroadcast(intent)
    }
}