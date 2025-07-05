package com.example.codecupapp


import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.codecupapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    //View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //ViewModels (state management)
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val ordersViewModel: OrdersViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    //UI states
    private var dots = arrayOfNulls<ImageView>(3)
    private var selectedAddressType: String = "Deliver"

    //View creation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    //After view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Load order state
        ordersViewModel.loadOrdersFromFirebase()

        //Dynamic time-based greeting
        binding.textGreeting.text = getGreeting()

        //Observe user name updates
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.textUsername.text = "${profile.name}."
        }

        //Delivery type selector, promotions, coffee menu, loyalty reward stamps
        setupAddressToggle()
        setupPromotions()
        setupCoffeeGrids()
        observeLoyaltyStamps(binding.loyaltyGridLayout)
    }

    //Return greeting based on time
    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            in 18..23 -> "Good Night,"
            else -> "Hello,"
        }
    }

    //Setup toggle between Deliver and Pickup
    private fun setupAddressToggle() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedAddressType = when (checkedId) {
                    R.id.btnDeliver -> "Deliver"
                    R.id.btnPickup -> "Pick up"
                    else -> "Deliver"
                }
                ordersViewModel.deliveryType.value = selectedAddressType
            }
        }
    }

    //Setup promotions carousel (images + dots)
    private fun setupPromotions() {
        val promoList = listOf(
            R.drawable.ruby_red_simple_embrace_the_journey_desktop_wallpaper,
            R.drawable.pastel_pink_retro_groovy_art_studio_typography_name_facebook_cover,
            R.drawable.pink_and_white_gradient__facebook_cover
        )

        binding.promoCarousel.adapter = PromoAdapter(promoList)
        addDots(promoList.size, 0)

        binding.promoCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                addDots(promoList.size, position)
            }
        })
    }

    /** 🔘 Add active/inactive indicator dots under carousel */
    private fun addDots(count: Int, selectedPosition: Int) {
        binding.dotsLayout.removeAllViews()
        dots = arrayOfNulls(count)

        for (i in 0 until count) {
            dots[i] = ImageView(requireContext()).apply {
                setImageResource(
                    if (i == selectedPosition) R.drawable.dot_active else R.drawable.dot_inactive
                )
            }

            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 0, 8, 0) }

            binding.dotsLayout.addView(dots[i], params)
        }
    }

    /** ☕ Setup coffee grid menus and navigation to Details */
    private fun setupCoffeeGrids() {
        binding.recyclerBestSeller.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = CoffeeAdapter(CoffeeRepository.bestSellers) { selectedItem ->
                navigateToDetails(selectedItem)
            }
        }

        binding.recyclerCoffee.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = CoffeeAdapter(CoffeeRepository.allCoffees) { selectedItem ->
                navigateToDetails(selectedItem)
            }
        }
    }

    /** 🎯 Navigate to detail screen with coffee info */
    private fun navigateToDetails(coffee: CoffeeItem) {
        val bundle = Bundle().apply {
            putString("coffeeName", coffee.name)
            putDouble("coffeePrice", coffee.price)
            putInt("coffeeImageResId", coffee.imageResId)
        }
        findNavController().navigate(R.id.detailsFragment, bundle)
    }

    /** 🏆 Observe stamp count and update loyalty stamp grid */
    private fun observeLoyaltyStamps(gridLayout: GridLayout) {
        loyaltyViewModel.stamps.observe(viewLifecycleOwner) { count ->
            gridLayout.removeAllViews()

            for (i in 1..8) {
                val cupIcon = ImageView(requireContext()).apply {
                    setImageResource(
                        if (i <= count) R.drawable.local_cafe_40px__1_
                        else R.drawable.local_cafe_40px
                    )
                    val size = 48.dp
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = size
                        height = size
                        setMargins(20, 20, 20, 20)
                        rowSpec = GridLayout.spec((i - 1) / 4)
                        columnSpec = GridLayout.spec((i - 1) % 4)
                    }
                }
                gridLayout.addView(cupIcon)
            }
        }
    }

    /** 🔧 Extension to convert dp to px */
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    /** 🔁 Always reload profile when returning to Home */
    override fun onResume() {
        super.onResume()
        profileViewModel.loadFromLocal(requireContext())
    }

    /** 🧹 Cleanup view binding */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
