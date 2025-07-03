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
import com.example.codecupapp.databinding.ActivityMainBinding
import com.example.codecupapp.data.CoffeePointsConfig

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Shared ViewModel
    private val cartViewModel: CartViewModel by viewModels()

    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Use ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔁 Return to HomeFragment when intent triggers
        if (intent.getBooleanExtra("startFromHome", false)) {
            Handler(Looper.getMainLooper()).post {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
                navHostFragment.navController.navigate(R.id.homeFragment)
            }
        }

        // ⚙️ Setup user config and load profile
        CoffeePointsConfig.initializeDefaults()

        ProfileRepository.loadUserProfile(
            onComplete = { Log.d("LoadProfile", "Profile loaded into UserData") },
            onError = { errorMsg -> Log.e("LoadProfile", "Failed to load profile: $errorMsg") }
        )

        setupToolbar()
        setupNavigation()
        observeCartUpdates()
        setupMenuButtons()
    }

    /** 🧭 Navigation and Bottom Nav setup */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

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

        // 🧭 Bottom nav item click
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(item.itemId)
            }
            true
        }

        // 👈 Back or Profile Icon
        binding.btnLeft.setOnClickListener {
            if (navController.currentDestination?.id in setOf(R.id.cartFragment, R.id.detailsFragment, R.id.redeemFragment)) {
                navController.navigateUp()
            } else {
                navController.navigate(R.id.profileFragment)
            }
        }

        // 🧠 Update nav visibility and icon dynamically
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuth = destination.id == R.id.authFragment
            val isHome = destination.id == R.id.homeFragment

            binding.toolbar.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.btnExpand.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.bottomNav.visibility = if (isAuth) View.GONE else View.VISIBLE

            binding.btnCartFloating.visibility = if (isHome) View.VISIBLE else View.GONE
            binding.badgeCount.visibility =
                if (isHome && binding.badgeCount.text.toString() != "0") View.VISIBLE else View.GONE

            // Change left button icon
            if (destination.id in setOf(R.id.cartFragment, R.id.detailsFragment, R.id.redeemFragment)) {
                binding.btnLeft.setImageResource(R.drawable.back_arrow)
            } else {
                binding.btnLeft.setImageResource(R.drawable.account_circle_40px)
            }

            // Highlight correct nav item
            when (destination.id) {
                R.id.homeFragment -> binding.bottomNav.menu.findItem(R.id.homeFragment).isChecked = true
                R.id.ordersFragment -> binding.bottomNav.menu.findItem(R.id.ordersFragment).isChecked = true
                R.id.rewardsFragment -> binding.bottomNav.menu.findItem(R.id.rewardsFragment).isChecked = true
                R.id.profileFragment -> binding.bottomNav.menu.findItem(R.id.profileFragment).isChecked = true
                R.id.redeemFragment -> binding.bottomNav.menu.findItem(R.id.rewardsFragment).isChecked = true
                else -> binding.bottomNav.menu.findItem(R.id.homeFragment).isChecked = true
            }
        }

        binding.btnLogo.setOnClickListener {
            navController.navigate(R.id.homeFragment)
        }

        binding.btnCart.setOnClickListener {
            navController.navigate(R.id.cartFragment)
        }

        binding.btnCartFloating.setOnClickListener {
            navController.navigate(R.id.cartFragment)
        }
    }

    /** 🛒 Observe cart data and update badge */
    private fun observeCartUpdates() {
        cartViewModel.cartItems.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            updateCartBadge(count)
        }
    }

    /** 🔔 Update floating cart badge visibility */
    fun updateCartBadge(count: Int) {
        val badgeText = count.toString()
        binding.badgeCount.text = badgeText

        val isHome = navController.currentDestination?.id == R.id.homeFragment
        binding.badgeCount.visibility = if (isHome && badgeText != "0") View.VISIBLE else View.GONE
    }


    /** 🔧 Top bar, expand toggle, and help/info buttons */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)

        binding.btnExpand.setOnClickListener {
            isExpanded = !isExpanded
            binding.expandedMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
        }
    }

    /** ❓ Info/help button click handlers */
    private fun setupMenuButtons() {
        binding.btnHelp.setOnClickListener {
            navController.navigate(R.id.helpFragment)
        }
        binding.btnInfo.setOnClickListener {
            navController.navigate(R.id.infoFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

