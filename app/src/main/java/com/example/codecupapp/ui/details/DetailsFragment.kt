package com.example.codecupapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.FragmentDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


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

        val args = arguments
        val itemName = args?.getString("coffeeName") ?: "Coffee"
        val imageResId = args?.getInt("coffeeImageResId") ?: R.drawable.local_cafe_40px__1_
        val price = args?.getDouble("coffeePrice") ?: 3.00
        val itemPoints = arguments?.getInt("coffeePoints") ?: 0

        basePrice = price // 🟢 update base price for size calculation
        binding.textCoffeeTitle.text = itemName
        binding.imageCoffee.setImageResource(imageResId)
        binding.textQuantity.text = quantity.toString()
        updateTotal()

        // Hide shot group for some drinks
        val hideShotFor = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")
        if (itemName in hideShotFor) {
            binding.shotGroup.parent?.let { (it as? View)?.visibility = View.GONE }
        }

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
        setupIconWithLabelGroup(
            binding.sizeGroup,
            listOf(
                Triple("Small", R.drawable.water_full_40px, 28f),
                Triple("Medium", R.drawable.water_full_40px, 36f),
                Triple("Large", R.drawable.water_full_40px, 44f)
            )
        ) { size = it
            updateTotal()
        }
        setupIconWithLabelGroup(
            binding.tempGroup,
            listOf(
                Triple("Hot", R.drawable.windshield_heat_front_40px, 32f),
                Triple("Iced", R.drawable.ac_unit_40px, 32f),
            ),
        ) { temperature = it }

        setupIconWithLabelGroup(
            binding.iceGroup,
            listOf(
                Triple("No Ice", R.drawable.block_40px, 32f),
                Triple("Some Ice", R.drawable.deployed_code_40px, 32f),
                Triple("Full Ice", R.drawable.deployed_code_40px, 39f)
            )
        ) { ice = it }
        setupIconWithLabelGroup(
            binding.shotGroup,
            listOf(
                Triple("Single", R.drawable.local_cafe_40px, 32f),
                Triple("Double", R.drawable.local_cafe_40px__1_, 32f)
            )
        ) { shot = it }
    }


    private fun setupIconWithLabelGroup(
        container: ViewGroup,
        options: List<Triple<String, Int, Float>>, // Label, Icon, IconSizeDp
        onSelected: (String) -> Unit
    ) {
        container.removeAllViews()
        var selected = options.first().first

        options.forEach { (label, iconRes, sizeDp) ->
            val imageView = ImageView(requireContext()).apply {
                setImageResource(iconRes)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(sizeDp).toInt(),
                    dpToPx(sizeDp).toInt()
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setColorFilter(Color.BLACK)
                tag = label
            }

            val textView = TextView(requireContext()).apply {
                text = label
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                maxLines = 2
                setPadding(4, 4, 4, 0)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    width = dpToPx(64f).toInt() // 👈 constrain label width to wrap under icon
                }
            }
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.maxLines = 2

            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dpToPx(12f).toInt()
                }
                setPadding(16, 16, 16, 16)
                isClickable = true
                isFocusable = true
                background = ContextCompat.getDrawable(context, R.drawable.chip_unselected_bg)

                setOnClickListener {
                    selected = label
                    onSelected(label)
                    updateIconGroupSelection(container, label)
                }

                addView(imageView)
                addView(textView)
            }

            container.addView(itemLayout)
        }

        onSelected(selected)
        updateIconGroupSelection(container, selected)
    }


    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    private fun updateIconGroupSelection(container: ViewGroup, selectedLabel: String) {
        for (i in 0 until container.childCount) {
            val layout = container.getChildAt(i) as LinearLayout
            val icon = layout.getChildAt(0) as ImageView
            val label = layout.getChildAt(1) as TextView
            val isSelected = icon.tag == selectedLabel

            val color = if (isSelected) R.color.redIcon else android.R.color.transparent
            val tint = if (isSelected) Color.WHITE else Color.BLACK

            layout.setBackgroundResource(if (isSelected) R.drawable.chip_selected_bg else R.drawable.chip_unselected_bg)
            layout.setPadding(24, 16, 24, 16)
            layout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), color)

            icon.setColorFilter(tint)
            label.setTextColor(tint)
        }
    }





    /** 🛒 Add to cart, open preview, go to cart actions */
    private fun setupCartActions() {
        binding.btnAddToCart.setOnClickListener {
            val sizeAdjustment = when (size) {
                "Medium" -> 1.5
                "Large" -> 3.0
                else -> 0.0
            }
            val adjustedPrice = basePrice + sizeAdjustment

            val item = CartItem(
                name = binding.textCoffeeTitle.text.toString(),
                shot = shot,
                temperature = temperature,
                size = size,
                ice = ice,
                quantity = quantity,
                unitPrice = adjustedPrice // ✅ Now includes size pricing!
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
        val adjustedPrice = when (size) {
            "Medium" -> basePrice + 1.5
            "Large" -> basePrice + 3.0
            else -> basePrice
        }

        val total = adjustedPrice * quantity
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
