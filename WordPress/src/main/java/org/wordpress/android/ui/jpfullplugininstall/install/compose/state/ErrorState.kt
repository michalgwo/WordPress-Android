package org.wordpress.android.ui.jpfullplugininstall.install.compose.state

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import org.wordpress.android.ui.compose.components.PrimaryButton
import org.wordpress.android.ui.compose.components.SecondaryButton
import org.wordpress.android.ui.compose.theme.AppTheme
import org.wordpress.android.ui.compose.unit.Margin
import org.wordpress.android.ui.jpfullplugininstall.install.JetpackFullPluginInstallViewModel.UiState

@Composable
fun ErrorState(
    uiState: UiState.Error,
    onRetryClick: () -> Unit,
    onContactSupportClick: () -> Unit,
    onDismissScreenClick: () -> Unit,
) = Box(
    Modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    with(uiState) {
        BaseState(
            uiState = uiState,
            onDismissScreenClick = onDismissScreenClick
        ) {
            PrimaryButton(
                text = stringResource(retryButtonText),
                onClick = onRetryClick,
                modifier = Modifier.padding(top = Margin.Large.value),
            )
            SecondaryButton(
                text = stringResource(contactSupportButtonText),
                onClick = onContactSupportClick,
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
@Preview(showBackground = true, device = Devices.PIXEL_4_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, device = Devices.PIXEL_4_XL, fontScale = 2f)
@Composable
private fun PreviewErrorState() {
    AppTheme {
        val uiState = UiState.Error()
        ErrorState(uiState, {}, {}, {})
    }
}
