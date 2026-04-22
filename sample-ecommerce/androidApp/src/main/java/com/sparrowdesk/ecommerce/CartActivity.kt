package com.sparrowdesk.ecommerce

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class CartActivity : AppCompatActivity() {

    private lateinit var emptyState: LinearLayout
    private lateinit var cartContent: LinearLayout
    private lateinit var cartItemsContainer: LinearLayout
    private lateinit var txtTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        emptyState = findViewById(R.id.emptyState)
        cartContent = findViewById(R.id.cartContent)
        cartItemsContainer = findViewById(R.id.cartItemsContainer)
        txtTotal = findViewById(R.id.txtTotal)

        // Browse products button (empty state)
        findViewById<MaterialButton>(R.id.btnBrowse).setOnClickListener { finish() }

        // Checkout
        findViewById<MaterialButton>(R.id.btnCheckout).setOnClickListener {
            CartManager.clear()
            Toast.makeText(this, getString(R.string.order_placed), Toast.LENGTH_LONG).show()
            finish()
        }

        // Chat support
        findViewById<MaterialButton>(R.id.btnChatSupport).setOnClickListener {
            startActivity(Intent(this, SupportChatActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCart()
    }

    private fun refreshCart() {
        val items = CartManager.getItems()

        if (items.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            cartContent.visibility = View.GONE
            return
        }

        emptyState.visibility = View.GONE
        cartContent.visibility = View.VISIBLE

        cartItemsContainer.removeAllViews()
        for (item in items) {
            cartItemsContainer.addView(createCartItemView(item))
        }

        txtTotal.text = CartManager.formattedTotal
    }

    private fun createCartItemView(product: Product): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 16)
        }

        // Emoji
        val emoji = TextView(this).apply {
            text = product.emoji
            textSize = 28f
            setPadding(0, 0, 24, 0)
        }
        row.addView(emoji)

        // Name + price column
        val infoColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        infoColumn.addView(TextView(this).apply {
            text = product.name
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.on_surface))
        })
        infoColumn.addView(TextView(this).apply {
            text = product.formattedPrice
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
        })
        row.addView(infoColumn)

        // Remove button
        val removeBtn = MaterialButton(
            this,
            null,
            com.google.android.material.R.attr.borderlessButtonStyle
        ).apply {
            text = getString(R.string.remove)
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.cart_badge))
            setOnClickListener {
                CartManager.remove(product)
                refreshCart()
            }
        }
        row.addView(removeBtn)

        return row
    }
}
