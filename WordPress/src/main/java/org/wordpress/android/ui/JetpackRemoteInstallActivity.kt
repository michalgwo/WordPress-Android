package org.wordpress.android.ui

import android.os.Bundle
import android.view.MenuItem
import org.wordpress.android.R
import org.wordpress.android.analytics.AnalyticsTracker.Stat.INSTALL_JETPACK_CANCELLED
import org.wordpress.android.databinding.JetpackRemoteInstallActivityBinding
import org.wordpress.android.ui.JetpackConnectionUtils.trackWithSource
import org.wordpress.android.ui.JetpackRemoteInstallFragment.Companion.TRACKING_SOURCE_KEY

class JetpackRemoteInstallActivity : LocaleAwareActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(JetpackRemoteInstallActivityBinding.inflate(layoutInflater)) {
            setContentView(root)
            setSupportActionBar(toolbarLayout.toolbarMain)
        }

        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.jetpack)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        trackWithSource(
            INSTALL_JETPACK_CANCELLED,
            intent.getSerializableExtra(TRACKING_SOURCE_KEY) as JetpackConnectionSource
        )
        super.onBackPressed()
    }
}
