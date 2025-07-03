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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.codecupapp.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val ordersViewModel: OrdersViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()



    private var dots = arrayOfNulls<ImageView>(3)
    var selectedAddressType: String = "Deliver" // Default selection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            in 18..23 -> "Good Night,"
            else -> "Hello,"
        }

        binding.textGreeting.text = greeting

        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.textUsername.text = "${profile.name}."
        }


        val promoList = listOf(
            R.drawable.promo1,
            R.drawable.promo1,
            R.drawable.promo1,
        )



        val gridLayout = view.findViewById<GridLayout>(R.id.loyaltyGridLayout)
        observeLoyaltyStamps(gridLayout)

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                selectedAddressType = when (checkedId) {
                    R.id.btnDeliver -> "Deliver"
                    R.id.btnPickup -> "Pick up"
                    else -> "Deliver"
                }

                ordersViewModel.deliveryType.value = selectedAddressType

            }
        }



        // Setup Carousel
        binding.promoCarousel.adapter = PromoAdapter(promoList)
        addDots(promoList.size, 0)

        binding.promoCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                addDots(promoList.size, position)
            }
        })

        binding.recyclerBestSeller.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerBestSeller.adapter = CoffeeAdapter(
            listOf("Americano", "Mocha", "Pumpkin Spice", "Taco Milktea", "Egg Coffee")
        ) { selectedCoffee ->
            openDetails(selectedCoffee)
        }

        binding.recyclerCoffee.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerCoffee.adapter = CoffeeAdapter(
            listOf("Latte", "Espresso", "Cappuccino", "Macchiato", "Drip Coffee")
        ) { selectedCoffee ->
            openDetails(selectedCoffee)
        }


    }

    private fun addDots(count: Int, selectedPosition: Int) {
        binding.dotsLayout.removeAllViews()
        dots = arrayOfNulls(count)

        for (i in 0 until count) {
            dots[i] = ImageView(requireContext())
            dots[i]?.setImageResource(
                if (i == selectedPosition) R.drawable.dot_active
                else R.drawable.dot_inactive
            )
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.dotsLayout.addView(dots[i], params)
        }
    }

    private fun openDetails(coffeeName: String) {
        val bundle = Bundle().apply {
            putString("coffeeName", coffeeName)
        }
        findNavController().navigate(R.id.detailsFragment, bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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






    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
