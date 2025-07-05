package com.example.codecupapp


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.codecupapp.data.SharedPrefsManager
import com.example.codecupapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // View binding for layout
    private lateinit var binding: ActivityMainBinding

    // Jetpack Navigation
    private lateinit var navController: NavController

    // Shared ViewModel for cart data
    private val cartViewModel: CartViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    // Menu toggle state
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout with ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If triggered from splash, return to homeFragment
        if (intent.getBooleanExtra("startFromHome", false)) {
            Handler(Looper.getMainLooper()).post {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
                navHostFragment.navController.navigate(R.id.homeFragment)
            }
        }


        //Load profile from SharedPreferences
        val localProfile = SharedPrefsManager.loadUserProfile(this)
        if (localProfile != null) {
            Log.d("MainActivity", "Loaded user profile from SharedPrefs: $localProfile")
            profileViewModel.setProfile(localProfile)
        } else {
            Log.w("MainActivity", "No local profile found, redirecting to login.")
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }


        setupToolbar()
        setupNavigation()
        observeCartUpdates()   //Sync cart badge with cart state
        setupMenuButtons()     // Set up expanding help/info buttons
    }

    // Configure Navigation UI and reactions
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
        binding.btnProfile.setOnClickListener {
            val current = navController.currentDestination?.id
            val showBack = current !in setOf(
                R.id.homeFragment,
                R.id.ordersFragment,
                R.id.rewardsFragment,
                R.id.profileFragment
            )
            if (showBack) {
                navController.navigateUp()
            } else {
                navController.navigate(R.id.profileFragment)
            }
        }



        // 🎯 Handle destination changes to show/hide UI parts
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuth = destination.id == R.id.authFragment
            val isHome = destination.id == R.id.homeFragment

            // 🟥 These fragments should show a simple top bar: just back + title
            val isSimpleTop = destination.id in setOf(
                R.id.cartFragment,
                R.id.detailsFragment,
                R.id.redeemFragment,
                R.id.helpFragment,
                R.id.infoFragment,
                R.id.orderSuccessFragment  // ⬅️ Include this
            )

            // 🧱 TOOLBAR + TOP BUTTONS VISIBILITY
            binding.toolbar.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.bottomNav.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnCartFloating.visibility = if (isHome) View.VISIBLE else View.GONE

            // 🔘 Cart badge
            binding.badgeCount.visibility =
                if (isHome && binding.badgeCount.text.toString() != "0") View.VISIBLE else View.GONE

            // 🔘 Hide these in detail-style fragments
            binding.btnLogo.visibility = if (isSimpleTop) View.GONE else View.VISIBLE
            binding.btnCart.visibility = if (isSimpleTop) View.GONE else View.VISIBLE
            binding.expandedMenu.visibility = View.GONE
            binding.btnExpandCollapsed.visibility = if (isSimpleTop) View.GONE else View.VISIBLE

            // 🔁 If simple top: hide profile button, use toolbar back arrow
// If top-level: show profile icon
// If deep fragment: show profile as back icon (custom logic)
            val isTopLevel = destination.id in setOf(
                R.id.homeFragment, R.id.ordersFragment, R.id.rewardsFragment, R.id.profileFragment
            )

            when {
                isSimpleTop -> {
                    binding.btnProfile.visibility = View.GONE
                }
                isTopLevel -> {
                    binding.btnProfile.visibility = View.VISIBLE
                    binding.btnProfile.setImageResource(R.drawable.account_circle_40px)
                }
                else -> {
                    binding.btnProfile.visibility = View.VISIBLE
                    binding.btnProfile.setImageResource(R.drawable.back_arrow)
                }
            }

            // 🏷️ TITLE (only shown on simple top pages like Help, Info, etc.)
            val title = when (destination.id) {
                R.id.cartFragment -> "Your Cart"
                R.id.detailsFragment -> "Details"
                R.id.redeemFragment -> "Redeem"
                R.id.helpFragment -> "Help"
                R.id.infoFragment -> "Information"
                R.id.orderSuccessFragment -> "Order Success"
                else -> ""
            }

            if (isSimpleTop) {
                binding.toolbarTitle.visibility = View.VISIBLE
                binding.toolbarTitle.text = title
            } else {
                binding.toolbarTitle.visibility = View.GONE
            }
            supportActionBar?.setDisplayShowTitleEnabled(false)


            // ✅ Bottom nav item selection (only top-level nav fragments)
            val navItem = when (destination.id) {
                R.id.homeFragment -> R.id.homeFragment
                R.id.ordersFragment -> R.id.ordersFragment
                R.id.rewardsFragment -> R.id.rewardsFragment
                R.id.profileFragment -> R.id.profileFragment
                R.id.redeemFragment -> R.id.rewardsFragment
                else -> null
            }
            navItem?.let {
                binding.bottomNav.menu.findItem(it).isChecked = true
            }
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

    // Observe cart and update badge
    private fun observeCartUpdates() {
        cartViewModel.cartItems.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            updateCartBadge(count)
        }
    }

    // Update badge visibility for floating cart icon
    fun updateCartBadge(count: Int) {
        val badgeText = count.toString()
        binding.badgeCount.text = badgeText

        val isHome = navController.currentDestination?.id == R.id.homeFragment
        binding.badgeCount.visibility = if (isHome && badgeText != "0") View.VISIBLE else View.GONE
    }

    // Setup toolbar with expand/collapse menu logic
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



    // Help and Info click handlers
    private fun setupMenuButtons() {
        binding.btnHelp.setOnClickListener {
            navController.navigate(R.id.helpFragment)

            // Collapse menu after navigating
            isExpanded = false
            binding.expandedMenu.visibility = View.GONE
            binding.btnExpandCollapsed.visibility = View.VISIBLE
        }

        binding.btnInfo.setOnClickListener {
            navController.navigate(R.id.infoFragment)

            // Collapse menu after navigating
            isExpanded = false
            binding.expandedMenu.visibility = View.GONE
            binding.btnExpandCollapsed.visibility = View.VISIBLE
        }
    }



    // Enable action bar up navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //Sync the orders from SharedPreferences to Firebase
    override fun onStop() {
        super.onStop()

    }
}
