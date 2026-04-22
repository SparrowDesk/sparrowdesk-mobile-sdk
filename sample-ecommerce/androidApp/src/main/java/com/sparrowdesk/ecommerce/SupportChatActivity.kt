package com.sparrowdesk.ecommerce

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.sparrowdesk.sdk.SparrowDeskCallback
import com.sparrowdesk.sdk.SparrowDeskConfig
import com.sparrowdesk.sdk.SparrowDeskSDK

class SupportChatActivity : AppCompatActivity() {

    private var sdk: SparrowDeskSDK? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_chat)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val container = findViewById<FrameLayout>(R.id.webviewContainer)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)

        // Initialize SDK
        val config = SparrowDeskConfig(
            domain = AppConfig.SPARROWDESK_DOMAIN,
            token = AppConfig.SPARROWDESK_TOKEN
        )
        val sparrowDesk = SparrowDeskSDK(config)

        // Show WebView only after widget is fully open
        sparrowDesk.onOpen(SparrowDeskCallback {
            runOnUiThread {
                loadingIndicator.visibility = View.GONE
                container.visibility = View.VISIBLE
            }
        })
        sparrowDesk.onClose(SparrowDeskCallback { finish() })

        // Attach to the container and open
        sparrowDesk.attach(this, container)
        sparrowDesk.openWidget()
        sparrowDesk.hideWidget()

        sdk = sparrowDesk

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        sdk?.destroy()
        sdk = null
    }
}
