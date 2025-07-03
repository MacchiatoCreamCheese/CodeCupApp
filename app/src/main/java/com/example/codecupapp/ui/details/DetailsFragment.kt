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

    // State Management
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
        initializeUI()
    }

    /** Entry point to initialize all behaviors and bindings */
    private fun initializeUI() {
        setupBottomSheet()
        setupQuantityButtons()
        setupOptionSelectors()
        setupCartActions()
        observeCartChanges()

        binding.textCoffeeTitle.text = arguments?.getString("coffeeName") ?: "Coffee"
        binding.textQuantity.text = quantity.toString()
        updateTotal()

        if (!cartViewModel.cartItems.value.isNullOrEmpty()) {
            showCartPreview(peek = true)
        }
    }

    /** Configure Bottom Sheet behavior with animation + half-peek */
    private fun setupBottomSheet() {
        _binding?.let { binding ->
            bottomSheetBehavior = BottomSheetBehavior.from(binding.cartPreviewSheet).apply {
                isHideable = true
                skipCollapsed = false
                isDraggable = true

                peekHeight = resources.getDimensionPixelSize(R.dimen.cart_peek_height)

                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        val show = newState != BottomSheetBehavior.STATE_HIDDEN
                        animateDimOverlay(show)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        _binding?.let { binding ->
                            binding.dimOverlay.alpha = slideOffset * 0.7f
                            if (slideOffset > 0f) {
                                binding.dimOverlay.visibility = View.VISIBLE
                                binding.btnAddToCart.visibility = View.GONE // Hide when bottom sheet slides up
                            } else {
                                binding.btnAddToCart.visibility = View.VISIBLE // Show again
                            }
                        }
                    }

                })
            }

            binding.dimOverlay.setOnClickListener {
                hideCartPreview()
            }
        }
    }


    /** ➕/➖ Handle quantity selection and price update */
    private fun setupQuantityButtons() {
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
    }

    /** 🔘 Setup shot/temp/size/ice options */
    private fun setupOptionSelectors() {
        setupOptionGroup(binding.shotGroup, listOf("Single", "Double")) { shot = it }
        setupOptionGroup(binding.tempGroup, listOf("Hot", "Iced")) { temperature = it }
        setupOptionGroup(binding.sizeGroup, listOf("Small", "Medium", "Large")) { size = it }
        setupOptionGroup(binding.iceGroup, listOf("No Ice", "Some Ice", "Full Ice")) {
            ice = it
        }
    }

    /** 🛒 Add to cart, open preview, go to cart actions */
    private fun setupCartActions() {
        binding.btnAddToCart.setOnClickListener {
            val item = CartItem(
                name = binding.textCoffeeTitle.text.toString(),
                shot = shot,
                temperature = temperature,
                size = size,
                ice = ice,
                quantity = quantity,
                unitPrice = basePrice
            )
            cartViewModel.dispatch(CartAction.AddItem(item))
            val totalCount = cartViewModel.cartItems.value.orEmpty().sumOf { it.quantity }
            (activity as? MainActivity)?.updateCartBadge(totalCount)
            showCartPreview()
        }

        binding.btnGoToCart.setOnClickListener {
            hideCartPreview()
            findNavController().navigate(R.id.cartFragment)
        }
    }

    /** 🧠 Observe cart updates to refresh the preview */
    private fun observeCartChanges() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            _binding?.let { binding ->
                binding.recyclerCartPreview.adapter = CartPreviewAdapter(cartItems)
                binding.recyclerCartPreview.layoutManager = LinearLayoutManager(requireContext())

                if (cartItems.isNotEmpty()) {
                    binding.cartPreviewSheet.visibility = View.VISIBLE
                } else {
                    hideCartPreview()
                }
            }
        }
    }

    /** 🎨 Set of buttons (e.g., size, temp) and selected state highlight */
    private fun setupOptionGroup(
        container: ViewGroup,
        options: List<String>,
        onSelected: (String) -> Unit
    ) {
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

    /** 🔴 Visually highlight selected option */
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

    /** 💵 Update price preview */
    private fun updateTotal() {
        val total = basePrice * quantity
        binding.textTotal.text = "Total: $%.2f".format(total)
    }

    /** 👁 Show preview sheet (expanded or peek) */
    private fun showCartPreview(peek: Boolean = false) {
        bottomSheetBehavior.state =
            if (peek) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
    }

    /** 👋 Hide preview and fade out dim overlay */
    private fun hideCartPreview() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    /** ✨ Animate the background dim overlay in/out */
    private fun animateDimOverlay(show: Boolean) {
        _binding?.let { binding ->
            val alpha = if (show) 0.7f else 0f
            binding.dimOverlay.animate()
                .alpha(alpha)
                .setDuration(300)
                .withStartAction {
                    if (show) binding.dimOverlay.visibility = View.VISIBLE
                }
                .withEndAction {
                    if (!show) binding.dimOverlay.visibility = View.GONE
                }
                .start()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
