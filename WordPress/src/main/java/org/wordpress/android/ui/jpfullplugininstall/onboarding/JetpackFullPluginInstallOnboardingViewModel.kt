package org.wordpress.android.ui.jpfullplugininstall.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.ui.accounts.HelpActivity
import org.wordpress.android.ui.jpfullplugininstall.onboarding.JetpackFullPluginInstallOnboardingViewModel.ActionEvent.ContactSupport
import org.wordpress.android.ui.jpfullplugininstall.onboarding.JetpackFullPluginInstallOnboardingViewModel.ActionEvent.Dismiss
import org.wordpress.android.ui.jpfullplugininstall.onboarding.JetpackFullPluginInstallOnboardingViewModel.ActionEvent.OpenTermsAndConditions
import org.wordpress.android.ui.mysite.SelectedSiteRepository
import org.wordpress.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class JetpackFullPluginInstallOnboardingViewModel @Inject constructor(
    private val uiStateMapper: JetpackFullPluginInstallOnboardingUiStateMapper,
    private val selectedSiteRepository: SelectedSiteRepository,
    private val analyticsTracker: JetpackFullPluginInstallOnboardingAnalyticsTracker,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
) : ScopedViewModel(bgDispatcher) {
    private val _uiState = MutableStateFlow<UiState>(UiState.None)
    val uiState = _uiState.asStateFlow()

    private val _actionEvents = MutableSharedFlow<ActionEvent>()
    val actionEvents = _actionEvents

    fun onScreenShown() {
        analyticsTracker.trackScreenShown()
        postUiState(uiStateMapper.mapLoaded())
    }

    fun onTermsAndConditionsClick() {
        postActionEvent(OpenTermsAndConditions)
    }

    fun onInstallFullPluginClick() {
        analyticsTracker.trackInstallButtonClick()
        // TODO tracking event
        // TODO open install full plugin screen when it's done
    }

    fun onContactSupportClick() {
        postActionEvent(
            ContactSupport(
                origin = HelpActivity.Origin.JETPACK_INSTALL_FULL_PLUGIN_ONBOARDING,
                selectedSite = selectedSiteRepository.getSelectedSite(),
            )
        )
    }

    fun onDismissScreenClick() {
        analyticsTracker.trackScreenDismissed()
        postActionEvent(Dismiss)
    }

    private fun postUiState(uiState: UiState) {
        launch {
            _uiState.update { uiState }
        }
    }

    private fun postActionEvent(actionEvent: ActionEvent) {
        launch {
            _actionEvents.emit(actionEvent)
        }
    }

    sealed class UiState {
        object None : UiState()
        data class Loaded(
            val siteName: String,
            val pluginNames: List<String>,
        ) : UiState()
    }

    sealed class ActionEvent {
        object OpenTermsAndConditions : ActionEvent()
        object InstallJPFullPlugin : ActionEvent()
        data class ContactSupport(
            val origin: HelpActivity.Origin,
            val selectedSite: SiteModel?,
        ) : ActionEvent()

        object Dismiss : ActionEvent()
    }
}
