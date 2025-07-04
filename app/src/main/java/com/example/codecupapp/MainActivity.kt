package com.example.codecupapp


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.codecupapp.databinding.ActivityMainBinding
import com.example.codecupapp.data.CoffeePointsConfig
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // View binding for layout
    private lateinit var binding: ActivityMainBinding

    // Jetpack Navigation
    private lateinit var navController: NavController

    // Shared ViewModel for cart data
    private val cartViewModel: CartViewModel by viewModels()

    // Menu toggle state
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inflate layout with ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔁 If triggered from splash, return to homeFragment
        if (intent.getBooleanExtra("startFromHome", false)) {
            Handler(Looper.getMainLooper()).post {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
                navHostFragment.navController.navigate(R.id.homeFragment)
            }
        }

        // ☕ Init default coffee point config (e.g., reward point rules)
        CoffeePointsConfig.initializeDefaults()

        // 🔄 Load user profile using coroutine
        lifecycleScope.launch {
            try {
                val profile = ProfileRepository.loadUserProfileSuspend()
                Log.d("MainActivity", "User profile loaded: $profile")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load profile: ${e.message}")
            }
        }

        setupToolbar()         // 🔧 Setup app top bar and expand buttons
        setupNavigation()      // 🧭 Setup nav controller, bottom bar, and routing logic
        observeCartUpdates()   // 🛒 Sync cart badge with cart state
        setupMenuButtons()     // ❓ Hook help/info buttons
    }

    /** 🧭 Configure Navigation UI and reactions */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level fragments for app bar config
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.ordersFragment,
                R.id.rewardsFragment,
                R.id.profileFragment,
                R.id.cartFragment,
                R.id.detailsFragment,
                R.id.redeemFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)

        // Handle bottom navigation taps
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(item.itemId)
            }
            true
        }

        // 👈 Handle left icon (back or profile)
        binding.btnLeft.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.cartFragment, R.id.detailsFragment, R.id.redeemFragment -> navController.navigateUp()
                else -> navController.navigate(R.id.profileFragment)
            }
        }

        // 🎯 Handle destination changes to show/hide UI parts
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuth = destination.id == R.id.authFragment
            val isHome = destination.id == R.id.homeFragment

            binding.toolbar.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnExpandCollapsed.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnExpandInsideMenu.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnInfo.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnHelp.visibility = if (isAuth) View.GONE else View.VISIBLE

            binding.bottomNav.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnCartFloating.visibility = if (isHome) View.VISIBLE else View.GONE
            binding.badgeCount.visibility =
                if (isHome && binding.badgeCount.text.toString() != "0") View.VISIBLE else View.GONE

            // Swap icon (👤 profile or ⬅ back)
            val isBackContext = destination.id in setOf(R.id.cartFragment, R.id.detailsFragment, R.id.redeemFragment)
            binding.btnLeft.setImageResource(
                if (isBackContext) R.drawable.back_arrow else R.drawable.account_circle_40px
            )

            // ✅ Highlight selected bottom nav item
            binding.bottomNav.menu.findItem(
                when (destination.id) {
                    R.id.homeFragment -> R.id.homeFragment
                    R.id.ordersFragment -> R.id.ordersFragment
                    R.id.rewardsFragment -> R.id.rewardsFragment
                    R.id.profileFragment -> R.id.profileFragment
                    R.id.redeemFragment -> R.id.rewardsFragment
                    else -> R.id.homeFragment
                }
            ).isChecked = true
        }

        // 🌟 Logo returns to home
        binding.btnLogo.setOnClickListener {
            navController.navigate(R.id.homeFragment)
        }

        // 🛒 Cart buttons
        binding.btnCart.setOnClickListener {
            navController.navigate(R.id.cartFragment)
        }
        binding.btnCartFloating.setOnClickListener {
            navController.navigate(R.id.cartFragment)
        }
    }

    /** 🛒 Observe cart and update badge */
    private fun observeCartUpdates() {
        cartViewModel.cartItems.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            updateCartBadge(count)
        }
    }

    /** 🔔 Update badge visibility for floating cart icon */
    fun updateCartBadge(count: Int) {
        val badgeText = count.toString()
        binding.badgeCount.text = badgeText

        val isHome = navController.currentDestination?.id == R.id.homeFragment
        binding.badgeCount.visibility = if (isHome && badgeText != "0") View.VISIBLE else View.GONE
    }

    /** 🔧 Setup toolbar with expand/collapse menu logic */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)

        // When collapsed chevron is tapped → show full menu
        binding.btnExpandCollapsed.setOnClickListener {
            isExpanded = true
            binding.btnExpandCollapsed.visibility = View.GONE
            binding.expandedMenu.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = 30f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }

        // When chevron inside expanded bar is tapped → collapse
        binding.btnExpandInsideMenu.setOnClickListener {
            isExpanded = false
            binding.expandedMenu.animate()
                .alpha(0f)
                .translationY(30f)
                .setDuration(200)
                .withEndAction {
                    binding.expandedMenu.visibility = View.GONE
                    binding.btnExpandCollapsed.visibility = View.VISIBLE
                }
                .start()
        }
    }



    /** ❓ Help and Info click handlers */
    private fun setupMenuButtons() {
        binding.btnHelp.setOnClickListener {
            navController.navigate(R.id.helpFragment)
        }
        binding.btnInfo.setOnClickListener {
            navController.navigate(R.id.infoFragment)
        }
    }

    /** 🔙 Enable action bar up navigation */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
