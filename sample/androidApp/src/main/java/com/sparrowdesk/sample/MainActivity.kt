package com.sparrowdesk.sample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.sparrowdesk.sdk.SparrowDeskConfig
import com.sparrowdesk.sdk.SparrowDeskSDK

class MainActivity : AppCompatActivity() {

    private var sdk: SparrowDeskSDK? = null
    private var isFullScreen = false

    private lateinit var editDomain: TextInputEditText
    private lateinit var editToken: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editName: TextInputEditText
    private lateinit var editTags: TextInputEditText
    private lateinit var controlButtons: LinearLayout
    private lateinit var configSection: ScrollView
    private lateinit var webviewContainer: FrameLayout
    private lateinit var txtStatus: TextView
    private lateinit var txtEvents: TextView

    private val exitFullScreenCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            // Back button: ask the widget to close; onClose callback restores layout.
            sdk?.closeWidget()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        editDomain = findViewById(R.id.editDomain)
        editToken = findViewById(R.id.editToken)
        editEmail = findViewById(R.id.editEmail)
        editName = findViewById(R.id.editName)
        editTags = findViewById(R.id.editTags)
        controlButtons = findViewById(R.id.controlButtons)
        configSection = findViewById(R.id.configSection)
        webviewContainer = findViewById(R.id.webviewContainer)
        txtStatus = findViewById(R.id.txtStatus)
        txtEvents = findViewById(R.id.txtEvents)

        onBackPressedDispatcher.addCallback(this, exitFullScreenCallback)

        // Load Widget
        findViewById<MaterialButton>(R.id.btnLoadHalf).setOnClickListener {
            loadWidget(fullScreen = false)
        }
        findViewById<MaterialButton>(R.id.btnLoadFull).setOnClickListener {
            loadWidget(fullScreen = true)
        }

        // Control buttons
        findViewById<MaterialButton>(R.id.btnOpen).setOnClickListener {
            sdk?.openWidget()
            logEvent("openWidget() called")
        }

        findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            sdk?.closeWidget()
            logEvent("closeWidget() called")
        }

        findViewById<MaterialButton>(R.id.btnHide).setOnClickListener {
            sdk?.hideWidget()
            logEvent("hideWidget() called")
        }

        findViewById<MaterialButton>(R.id.btnShow).setOnClickListener {
            sdk?.show()
            logEvent("show() called")
        }

        findViewById<MaterialButton>(R.id.btnHideWebview).setOnClickListener {
            sdk?.hide()
            logEvent("hide() called")
        }

        findViewById<MaterialButton>(R.id.btnStatus).setOnClickListener {
            sdk?.getStatus { status ->
                runOnUiThread {
                    txtStatus.text = "Status: ${status.name}"
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnDestroy).setOnClickListener {
            sdk?.destroy()
            sdk = null
            webviewContainer.visibility = View.GONE
            controlButtons.visibility = View.GONE
            configSection.visibility = View.VISIBLE
            isFullScreen = false
            exitFullScreenCallback.isEnabled = false
            logEvent("destroy() called — SDK cleaned up")
            Toast.makeText(this, "SDK destroyed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWidget(fullScreen: Boolean) {
        val domain = editDomain.text?.toString()?.trim() ?: ""
        val token = editToken.text?.toString()?.trim() ?: ""

        if (domain.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Domain and Token are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Destroy previous instance if any
        sdk?.destroy()

        // Create SDK
        val config = SparrowDeskConfig(domain = domain, token = token, debug = true)
        val newSdk = SparrowDeskSDK(config)

        // Set contact fields if provided
        val contactFields = mutableMapOf<String, String>()
        editEmail.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
            contactFields["email"] = it
        }
        editName.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
            contactFields["name"] = it
        }
        if (contactFields.isNotEmpty()) {
            newSdk.setContactFields(contactFields)
            logEvent("setContactFields: $contactFields")
        }

        // Set tags if provided
        val tagsText = editTags.text?.toString()?.trim() ?: ""
        if (tagsText.isNotEmpty()) {
            val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            newSdk.setTags(tags)
            logEvent("setTags: $tags")
        }

        // Register event callbacks — reveal the WebView only after the widget is open & ready
        newSdk.onOpen {
            runOnUiThread {
                logEvent("Event: Widget OPENED")
                revealWebView(fullScreen)
            }
        }
        newSdk.onClose {
            runOnUiThread {
                logEvent("Event: Widget CLOSED")
                if (isFullScreen) restoreConfigLayout()
            }
        }

        // Attach WebView but keep it hidden until the widget is open & ready
        webviewContainer.removeAllViews()
        webviewContainer.visibility = View.GONE
        newSdk.attach(this, webviewContainer)

        // Queue open — revealWebView() will fire from the onOpen callback above
        newSdk.openWidget()
        logEvent("openWidget() queued — auto-open on load")

        sdk = newSdk
        controlButtons.visibility = View.VISIBLE
        val mode = if (fullScreen) "full screen" else "half screen"
        logEvent("Widget loaded ($mode, domain=$domain)")
        Toast.makeText(this, "Widget loading ($mode)…", Toast.LENGTH_SHORT).show()
    }

    private fun revealWebView(fullScreen: Boolean) {
        webviewContainer.visibility = View.VISIBLE
        isFullScreen = fullScreen
        if (fullScreen) {
            configSection.visibility = View.GONE
            exitFullScreenCallback.isEnabled = true
        } else {
            configSection.visibility = View.VISIBLE
            exitFullScreenCallback.isEnabled = false
        }
    }

    private fun restoreConfigLayout() {
        configSection.visibility = View.VISIBLE
        webviewContainer.visibility = View.GONE
        isFullScreen = false
        exitFullScreenCallback.isEnabled = false
        logEvent("Exited full screen")
    }

    private fun logEvent(message: String) {
        val current = txtEvents.text?.toString() ?: ""
        val lines = current.split("\n").takeLast(4)
        txtEvents.text = (lines + message).joinToString("\n")
    }

    override fun onDestroy() {
        sdk?.destroy()
        sdk = null
        super.onDestroy()
    }
}
