package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MasarAppWidgetProvider : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = MasarGlanceAppWidget()

    companion object {
        private val widgetScope = CoroutineScope(Dispatchers.Default)

        fun refreshAllWidgets(context: Context) {
            val appContext = context.applicationContext
            widgetScope.launch {
                runCatching {
                    MasarGlanceAppWidget().updateAll(appContext)
                }
            }
        }

        fun requestPinMasarWidget(context: Context): Boolean {
            return runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    if (appWidgetManager.isRequestPinAppWidgetSupported) {
                        val provider = ComponentName(context, MasarAppWidgetProvider::class.java)
                        return appWidgetManager.requestPinAppWidget(provider, null, null)
                    }
                }
                false
            }.getOrDefault(false)
        }
    }
}
