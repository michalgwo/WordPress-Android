package org.wordpress.android.ui.stats.refresh.lists.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.wordpress.android.R
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.stats.insights.AllTimeInsightsStore
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.ui.stats.StatsTimeframe.INSIGHTS
import org.wordpress.android.ui.stats.refresh.StatsActivity
import org.wordpress.android.ui.stats.refresh.lists.widget.StatsWidgetConfigureFragment.ViewType.ALL_TIME_VIEWS
import org.wordpress.android.ui.stats.refresh.lists.widget.StatsWidgetConfigureViewModel.Color.LIGHT
import org.wordpress.android.ui.stats.refresh.utils.toFormattedString
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.viewmodel.ResourceProvider
import javax.inject.Inject

private const val EMPTY_VALUE = "-"

class AllTimeWidgetUpdater
@Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper,
    private val siteStore: SiteStore,
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val resourceProvider: ResourceProvider,
    private val allTimeStore: AllTimeInsightsStore,
    private val widgetUtils: WidgetUtils
) : WidgetUpdater {
    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val showColumns = widgetUtils.isWidgetWiderThanLimit(appWidgetManager, appWidgetId)
        val colorMode = appPrefsWrapper.getAppWidgetColor(appWidgetId) ?: LIGHT
        val siteId = appPrefsWrapper.getAppWidgetSiteId(appWidgetId)
        val siteModel = siteStore.getSiteBySiteId(siteId)
        val networkAvailable = networkUtilsWrapper.isNetworkAvailable()
        val views = RemoteViews(context.packageName, widgetUtils.getLayout(showColumns, colorMode))
        views.setTextViewText(R.id.widget_title, resourceProvider.getString(R.string.stats_insights_all_time_stats))
        widgetUtils.setSiteIcon(siteModel, context, views, appWidgetId)
        siteModel?.let {
            views.setOnClickPendingIntent(
                    R.id.widget_title,
                    widgetUtils.getPendingSelfIntent(context, siteModel.id, INSIGHTS)
            )
        }
        if (networkAvailable && siteModel != null) {
            if (showColumns) {
                views.setOnClickPendingIntent(
                        R.id.widget_content,
                        widgetUtils.getPendingSelfIntent(context, siteModel.id, INSIGHTS)
                )
                showColumns(appWidgetManager, appWidgetId, views, siteModel)
            } else {
                views.setPendingIntentTemplate(R.id.widget_content, getPendingTemplate(context))
                widgetUtils.showList(
                        appWidgetManager,
                        views,
                        context,
                        appWidgetId,
                        colorMode,
                        siteId,
                        ALL_TIME_VIEWS,
                        showColumns
                )
            }
        } else {
            widgetUtils.showError(appWidgetManager, views, appWidgetId, networkAvailable, resourceProvider, context)
        }
    }

    override fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val viewsWidget = ComponentName(context, StatsAllTimeWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(viewsWidget)
        for (appWidgetId in allWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun showColumns(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        views: RemoteViews,
        site: SiteModel
    ) {
        views.setViewVisibility(R.id.widget_content, View.VISIBLE)
        views.setViewVisibility(R.id.widget_error, View.GONE)
        loadCachedAllTimeInsights(appWidgetManager, appWidgetId, site, views)
        GlobalScope.launch {
            runBlocking {
                allTimeStore.fetchAllTimeInsights(site)
            }
            loadCachedAllTimeInsights(appWidgetManager, appWidgetId, site, views)
        }
    }

    private fun loadCachedAllTimeInsights(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        site: SiteModel,
        views: RemoteViews
    ) {
        val allTimeInsights = allTimeStore.getAllTimeInsights(site)
        views.setTextViewText(R.id.first_block_title, resourceProvider.getString(R.string.stats_views))
        views.setTextViewText(R.id.first_block_value, allTimeInsights?.views?.toFormattedString() ?: EMPTY_VALUE)
        views.setTextViewText(R.id.second_block_title, resourceProvider.getString(R.string.stats_visitors))
        views.setTextViewText(R.id.second_block_value, allTimeInsights?.visitors?.toFormattedString() ?: EMPTY_VALUE)
        views.setTextViewText(R.id.third_block_title, resourceProvider.getString(R.string.posts))
        views.setTextViewText(R.id.third_block_value, allTimeInsights?.posts?.toFormattedString() ?: EMPTY_VALUE)
        views.setTextViewText(R.id.fourth_block_title, resourceProvider.getString(R.string.stats_insights_best_ever))
        views.setTextViewText(
                R.id.fourth_block_value,
                allTimeInsights?.viewsBestDayTotal?.toFormattedString() ?: EMPTY_VALUE
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getPendingTemplate(context: Context): PendingIntent {
        val intent = Intent(context, StatsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun delete(appWidgetId: Int) {
        appPrefsWrapper.removeAppWidgetColorModeId(appWidgetId)
        appPrefsWrapper.removeAppWidgetSiteId(appWidgetId)
    }
}
