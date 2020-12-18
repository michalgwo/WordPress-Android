package org.wordpress.android.ui.jetpack.common.viewholders

import android.view.ViewGroup
import kotlinx.android.synthetic.main.backup_download_list_description_item.*
import org.wordpress.android.R
import org.wordpress.android.ui.jetpack.common.JetpackListItemState
import org.wordpress.android.ui.jetpack.common.JetpackListItemState.DescriptionState
import org.wordpress.android.ui.utils.UiHelpers

class DescriptionViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder(R.layout.backup_download_list_description_item, parent) {
    override fun onBind(itemUiState: JetpackListItemState) {
        val descriptionState = itemUiState as DescriptionState
        with(uiHelpers) {
            backup_download_description.text = getTextOfUiString(itemView.context, descriptionState.text)
        }
    }
}
