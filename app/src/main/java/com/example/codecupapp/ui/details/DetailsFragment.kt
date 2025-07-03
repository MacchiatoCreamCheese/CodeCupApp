package com.example.codecupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.FragmentDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>


    private var quantity = 1
    private var shot = "Single"
    private var temperature = "Hot"
    private var size = "Medium"
    private var ice = "Full Ice"
    private var basePrice = 3.00

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        bottomSheetBehavior = BottomSheetBehavior.from(binding.cartPreviewSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isDraggable = true

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    binding.dimOverlay.visibility =
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.dimOverlay.alpha = slideOffset * 0.7f
                    if (slideOffset > 0f) {
                        binding.dimOverlay.visibility = View.VISIBLE
                    }
                }
            })
        }

        binding.dimOverlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }


        val coffeeName = arguments?.getString("coffeeName") ?: "Coffee"
        binding.textCoffeeTitle.text = coffeeName
        binding.textQuantity.text = quantity.toString()

        // Setup option buttons
        setupOptionGroup(binding.shotGroup, listOf("Single", "Double")) { selected -> shot = selected }
        setupOptionGroup(binding.tempGroup, listOf("Hot", "Iced")) { selected -> temperature = selected }
        setupOptionGroup(binding.sizeGroup, listOf("Small", "Medium", "Large")) { selected -> size = selected }
        setupOptionGroup(binding.iceGroup, listOf("No Ice", "Some Ice", "Full Ice")) { selected -> ice = selected }

        binding.btnPlus.setOnClickListener {
            quantity++
            binding.textQuantity.text = quantity.toString()
            updateTotal()
        }

        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.textQuantity.text = quantity.toString()
                updateTotal()
            }
        }

        updateTotal()

        // ✅ Handle Add to Cart and show CartBottomSheet
        binding.btnAddToCart.setOnClickListener {
            val item = CartItem(
                name = coffeeName,
                shot = shot,
                temperature = temperature,
                size = size,
                ice = ice,
                quantity = quantity,
                unitPrice = basePrice
            )

            cartViewModel.dispatch(CartAction.AddItem(item))

            binding.recyclerCartPreview.adapter = CartPreviewAdapter(cartViewModel.cartItems.value.orEmpty())
            binding.recyclerCartPreview.layoutManager = LinearLayoutManager(requireContext())

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


        binding.btnGoToCart.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.cartFragment)
        }
    }

    private fun setupOptionGroup(container: ViewGroup, options: List<String>, onSelected: (String) -> Unit) {
        container.removeAllViews()
        options.forEach { option ->
            val button = Button(requireContext()).apply {
                text = option
                setOnClickListener {
                    onSelected(option)
                    updateSelection(container, option)
                }
            }
            container.addView(button)
        }
        onSelected(options.first())
        updateSelection(container, options.first())
    }

    private fun updateSelection(container: ViewGroup, selected: String) {
        for (i in 0 until container.childCount) {
            val btn = container.getChildAt(i) as Button
            btn.setBackgroundColor(
                if (btn.text == selected)
                    resources.getColor(R.color.redIcon, requireContext().theme)
                else
                    resources.getColor(R.color.greyIcon, requireContext().theme)
            )
        }
    }

    private fun updateTotal() {
        val total = basePrice * quantity
        binding.textTotal.text = "Total: $%.2f".format(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

