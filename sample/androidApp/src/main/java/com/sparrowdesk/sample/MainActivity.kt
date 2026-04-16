package com.sparrowdesk.sample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.sparrowdesk.sdk.SparrowDeskConfig
import com.sparrowdesk.sdk.SparrowDeskSDK

class MainActivity : AppCompatActivity() {

    private var sdk: SparrowDeskSDK? = null

    private lateinit var editDomain: TextInputEditText
    private lateinit var editToken: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editName: TextInputEditText
    private lateinit var editTags: TextInputEditText
    private lateinit var controlButtons: LinearLayout
    private lateinit var webviewContainer: FrameLayout
    private lateinit var txtStatus: TextView
    private lateinit var txtEvents: TextView

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
        webviewContainer = findViewById(R.id.webviewContainer)
        txtStatus = findViewById(R.id.txtStatus)
        txtEvents = findViewById(R.id.txtEvents)

        // Load Widget
        findViewById<MaterialButton>(R.id.btnLoad).setOnClickListener {
            loadWidget()
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
            logEvent("destroy() called — SDK cleaned up")
            Toast.makeText(this, "SDK destroyed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWidget() {
        val domain = editDomain.text?.toString()?.trim() ?: ""
        val token = editToken.text?.toString()?.trim() ?: ""

        if (domain.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Domain and Token are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Destroy previous instance if any
        sdk?.destroy()

        // Create SDK
        val config = SparrowDeskConfig(domain = domain, token = token)
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

        // Register event callbacks
        newSdk.onOpen {
            runOnUiThread { logEvent("Event: Widget OPENED") }
        }
        newSdk.onClose {
            runOnUiThread { logEvent("Event: Widget CLOSED") }
        }

        // Attach WebView
        webviewContainer.removeAllViews()
        webviewContainer.visibility = View.VISIBLE
        newSdk.attach(this, webviewContainer)

        sdk = newSdk
        controlButtons.visibility = View.VISIBLE
        logEvent("Widget loaded (domain=$domain)")
        Toast.makeText(this, "Widget loaded!", Toast.LENGTH_SHORT).show()
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
