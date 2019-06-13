@file:JvmName("StatsWidgetConfigureViewModelKt")

package org.wordpress.android.ui.stats.refresh.lists.widget

import androidx.annotation.LayoutRes
import kotlinx.coroutines.runBlocking
import org.wordpress.android.R
import org.wordpress.android.fluxc.model.stats.VisitsModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.stats.insights.TodayInsightsStore
import org.wordpress.android.ui.stats.refresh.lists.widget.StatsWidgetConfigureViewModel.Color
import org.wordpress.android.ui.stats.refresh.utils.toFormattedString
import org.wordpress.android.viewmodel.ResourceProvider
import javax.inject.Inject

class TodayWidgetListViewModel
@Inject constructor(
    private val siteStore: SiteStore,
    private val todayInsightsStore: TodayInsightsStore,
    private val resourceProvider: ResourceProvider
) {
    private var siteId: Long? = null
    private var colorMode: Color = Color.LIGHT
    private var appWidgetId: Int? = null
    private val mutableData = mutableListOf<TodayItemUiModel>()
    val data: List<TodayItemUiModel> = mutableData
    fun start(siteId: Long, colorMode: Color, appWidgetId: Int) {
        this.siteId = siteId
        this.colorMode = colorMode
        this.appWidgetId = appWidgetId
    }

    fun onDataSetChanged(onError: (appWidgetId: Int) -> Unit) {
        siteId?.apply {
            val site = siteStore.getSiteBySiteId(this)
            if (site != null) {
                runBlocking {
                    todayInsightsStore.fetchTodayInsights(site)
                }
                todayInsightsStore.getTodayInsights(site)?.let { visitsAndViewsModel ->
                    val uiModels = buildListItemUiModel(visitsAndViewsModel, this)
                    if (uiModels != data) {
                        mutableData.clear()
                        mutableData.addAll(uiModels)
                    }
                }
            } else {
                appWidgetId?.let { nonNullAppWidgetId ->
                    onError(nonNullAppWidgetId)
                }
            }
        }
    }

    private fun buildListItemUiModel(
        domainModel: VisitsModel,
        localSiteId: Long
    ): List<TodayItemUiModel> {
        val layout = when (colorMode) {
            Color.DARK -> R.layout.stats_views_widget_item_dark
            Color.LIGHT -> R.layout.stats_views_widget_item_light
        }
        return listOf(
                TodayItemUiModel(
                        layout,
                        localSiteId,
                        resourceProvider.getString(R.string.stats_views),
                        domainModel.views.toFormattedString()
                ),
                TodayItemUiModel(
                        layout,
                        localSiteId,
                        resourceProvider.getString(R.string.stats_visitors),
                        domainModel.visitors.toFormattedString()
                ),
                TodayItemUiModel(
                        layout,
                        localSiteId,
                        resourceProvider.getString(R.string.posts),
                        domainModel.posts.toFormattedString()
                ),
                TodayItemUiModel(
                        layout,
                        localSiteId,
                        resourceProvider.getString(R.string.stats_comments),
                        domainModel.comments.toFormattedString()
                )
        )
    }

    data class TodayItemUiModel(
        @LayoutRes val layout: Int,
        val localSiteId: Long,
        val key: String,
        val value: String
    )
}
