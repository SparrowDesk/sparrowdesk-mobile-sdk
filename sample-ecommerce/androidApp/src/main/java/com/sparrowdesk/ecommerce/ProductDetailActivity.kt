package com.sparrowdesk.ecommerce

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        val product = Product.findById(productId)

        if (product == null) {
            finish()
            return
        }

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = product.name
        toolbar.setNavigationOnClickListener { finish() }

        // Product info
        findViewById<FrameLayout>(R.id.productImageBg)
            .setBackgroundColor(ContextCompat.getColor(this, product.bgColor))
        findViewById<TextView>(R.id.txtEmoji).text = product.emoji
        findViewById<TextView>(R.id.txtName).text = product.name
        findViewById<TextView>(R.id.txtPrice).text = product.formattedPrice
        findViewById<TextView>(R.id.txtDescription).text = product.description

        // Add to cart
        findViewById<MaterialButton>(R.id.btnAddToCart).setOnClickListener {
            CartManager.add(product)
            Toast.makeText(this, getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show()
        }

        // Chat support
        findViewById<MaterialButton>(R.id.btnChatSupport).setOnClickListener {
            startActivity(Intent(this, SupportChatActivity::class.java))
        }
    }
}
