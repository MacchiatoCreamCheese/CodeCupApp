package com.example.codecupapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.codecupapp.data.CartData.cartItems
import com.example.codecupapp.data.CoffeePointsConfig
import com.example.codecupapp.ui.theme.CodeCupAppTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var expandButton: ImageButton
    private lateinit var toolbar: Toolbar
    private lateinit var btnCartFloating: FloatingActionButton
    private lateinit var badgeCount: TextView

    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        CoffeePointsConfig.initializeDefaults()





        // View Initialization
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val btnLeft = findViewById<ImageButton>(R.id.btnLeft)
        val btnLogo = findViewById<ImageButton>(R.id.btnLogo)
        val btnCart = findViewById<ImageButton>(R.id.btnCart)

        btnLogo.setOnClickListener {
            navController.navigate(R.id.homeFragment)
        }

        btnCart.setOnClickListener {
            navController.navigate(R.id.cartFragment)
        }
        bottomNav = findViewById(R.id.bottom_nav)
        expandButton = findViewById(R.id.btnExpand)
        btnCartFloating = findViewById(R.id.btnCartFloating)
        badgeCount = findViewById(R.id.badgeCount)



        // Setup Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.ordersFragment,
                R.id.rewardsFragment,
                R.id.profileFragment,
                R.id.cartFragment,      // If you want no Up arrow here
                R.id.detailsFragment,   // If you want no Up arrow here
                R.id.redeemFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)

        // Bottom Navigation links
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(item.itemId)
            }
            true
        }

        // Expand Button toggles nav bar
        expandButton.setOnClickListener {
            isExpanded = !isExpanded
            val expandedMenu = findViewById<LinearLayout>(R.id.expandedMenu)
            expandedMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
        }


        // Floating Cart Button click
        btnCartFloating.setOnClickListener {
            navController.navigate(R.id.cartFragment)
        }

        // Show/Hide NavBar, Cart Button, Back Arrow
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNav = destination.id != R.id.authFragment
            bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE
            expandButton.visibility = if (destination.id == R.id.authFragment) View.GONE else View.VISIBLE

            btnCartFloating.visibility =
                if (destination.id == R.id.homeFragment) View.VISIBLE else View.GONE
            badgeCount.visibility =
                if (destination.id == R.id.homeFragment && badgeCount.text.toString() != "0") View.VISIBLE else View.GONE

            val showToolbar = destination.id != R.id.authFragment
            toolbar.visibility = if (showToolbar) View.VISIBLE else View.GONE
            expandButton.visibility = if (showToolbar) View.VISIBLE else View.GONE

            if (destination.id == R.id.cartFragment || destination.id == R.id.detailsFragment || destination.id == R.id.redeemFragment) {
                btnLeft.setImageResource(R.drawable.back_arrow)
                btnLeft.setOnClickListener { navController.navigateUp() }
            } else {
                btnLeft.setImageResource(R.drawable.account_circle_40px)
                btnLeft.setOnClickListener { navController.navigate(R.id.profileFragment) }
            }


            when (destination.id) {
                R.id.homeFragment -> bottomNav.menu.findItem(R.id.homeFragment).isChecked = true
                R.id.ordersFragment -> bottomNav.menu.findItem(R.id.ordersFragment).isChecked = true
                R.id.rewardsFragment -> bottomNav.menu.findItem(R.id.rewardsFragment).isChecked = true
                R.id.profileFragment -> bottomNav.menu.findItem(R.id.profileFragment).isChecked = true
                R.id.redeemFragment-> bottomNav.menu.findItem(R.id.rewardsFragment).isChecked = true
                else -> {
                    bottomNav.menu.findItem(R.id.homeFragment).isChecked = true
                }
            }
        }





        findViewById<ImageButton>(R.id.btnHelp).setOnClickListener {
            navController.navigate(R.id.helpFragment)
        }

        findViewById<ImageButton>(R.id.btnInfo).setOnClickListener {
            navController.navigate(R.id.infoFragment)
        }

        val prefs = getSharedPreferences("AppPrefs", 0)
        if (prefs.getBoolean("logged_in", false)) {
            UserData.name = prefs.getString("username", "Guest") ?: "Guest"
            UserData.email = prefs.getString("email", "guest@example.com") ?: "guest@example.com"

            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            val navController = navHost.navController

            // Force Navigate to Home if logged in
            navController.navigate(R.id.homeFragment)
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    fun updateCartBadge(count: Int) {
        badgeCount.text = count.toString()

        val currentDest = navController.currentDestination?.id

        val shouldShowBadge = currentDest == R.id.homeFragment && count > 0
        badgeCount.visibility = if (shouldShowBadge) View.VISIBLE else View.GONE
    }



}
