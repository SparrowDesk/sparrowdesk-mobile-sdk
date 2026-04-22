package com.sparrowdesk.ecommerce

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)

        // Product card clicks
        val cards = listOf(
            R.id.card1 to 1,
            R.id.card2 to 2,
            R.id.card3 to 3,
            R.id.card4 to 4
        )
        for ((cardId, productId) in cards) {
            findViewById<MaterialCardView>(cardId).setOnClickListener {
                startActivity(
                    Intent(this, ProductDetailActivity::class.java)
                        .putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId)
                )
            }
        }

        // Toolbar cart menu
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_cart) {
                startActivity(Intent(this, CartActivity::class.java))
                true
            } else false
        }

        // Chat FAB
        findViewById<ExtendedFloatingActionButton>(R.id.fabChat).setOnClickListener {
            startActivity(Intent(this, SupportChatActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val count = CartManager.getCount()
        val cartItem = toolbar.menu.findItem(R.id.action_cart)
        cartItem?.title = if (count > 0) "Cart ($count)" else "Cart"
    }
}
