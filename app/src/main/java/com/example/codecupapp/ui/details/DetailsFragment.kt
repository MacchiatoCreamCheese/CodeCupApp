package com.example.codecupapp

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    // Selected options and price
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

    // Main initialization
    private fun initializeUI() {
        setupBottomSheet()
        setupQuantityButtons()
        setupOptionSelectors()
        setupCartActions()
        observeCartChanges()
        loadItemData()
    }

    private fun loadItemData() {
        val args = arguments
        val itemName = args?.getString("coffeeName") ?: "Coffee"
        val imageResId = args?.getInt("coffeeImageResId") ?: R.drawable.local_cafe_40px__1_
        val price = args?.getDouble("coffeePrice") ?: 3.00
        basePrice = price

        binding.textCoffeeTitle.text = itemName
        binding.imageCoffee.setImageResource(imageResId)
        binding.textQuantity.text = quantity.toString()
        updateTotal()

        val skipShotFor = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")
        if (itemName in skipShotFor) {
            binding.shotGroup.parent?.let { (it as? View)?.visibility = View.GONE }
        }

        if (!cartViewModel.cartItems.value.isNullOrEmpty()) {
            showCartPreview(peek = true)
        }
    }

    // Quantity controls
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

    // Cart interaction
    private fun setupCartActions() {
        binding.btnAddToCart.setOnClickListener {
            val itemName = binding.textCoffeeTitle.text.toString()
            val skipShot = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")
                .contains(itemName)

            val sizeAdjustment = when (size) {
                "Medium" -> 1.5
                "Large" -> 3.0
                else -> 0.0
            }

            val item = CartItem(
                name = itemName,
                shot = if (skipShot) "" else shot,
                temperature = temperature,
                size = size,
                ice = ice,
                quantity = quantity,
                unitPrice = basePrice + sizeAdjustment
            )

            cartViewModel.dispatch(CartAction.AddItem(item))
            val totalItems = cartViewModel.cartItems.value.orEmpty().sumOf { it.quantity }
            (activity as? MainActivity)?.updateCartBadge(totalItems)

            showCartPreview()
        }

        binding.btnGoToCart.setOnClickListener {
            hideCartPreview()
            findNavController().navigate(R.id.cartFragment)
        }
    }

    private fun observeCartChanges() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            binding.recyclerCartPreview.adapter = CartPreviewAdapter(items)
            binding.recyclerCartPreview.layoutManager = LinearLayoutManager(requireContext())

            if (items.isNotEmpty()) {
                binding.cartPreviewSheet.visibility = View.VISIBLE
            } else {
                hideCartPreview()
            }
        }
    }

    // Option selectors (shot, temp, size, ice)
    private fun setupOptionSelectors() {
        setupIconGroup(binding.sizeGroup, listOf(
            Triple("Small", R.drawable.water_full_40px, 28f),
            Triple("Medium", R.drawable.water_full_40px, 36f),
            Triple("Large", R.drawable.water_full_40px, 44f)
        )) {
            size = it
            updateTotal()
        }

        setupIconGroup(binding.tempGroup, listOf(
            Triple("Iced", R.drawable.ac_unit_40px, 32f),
            Triple("Hot", R.drawable.windshield_heat_front_40px, 32f)
        )) {
            temperature = it
            updateIceGroupAvailability(it == "Hot")
        }

        setupIconGroup(binding.iceGroup, listOf(
            Triple("No Ice", R.drawable.block_40px, 32f),
            Triple("Some Ice", R.drawable.deployed_code_40px, 32f),
            Triple("Full Ice", R.drawable.deployed_code_40px, 39f)
        )) {
            ice = it
        }

        setupIconGroup(binding.shotGroup, listOf(
            Triple("Single", R.drawable.local_cafe_40px, 32f),
            Triple("Double", R.drawable.local_cafe_40px__1_, 32f)
        )) {
            shot = it
        }
    }

    private fun updateIceGroupAvailability(disable: Boolean) {
        for (i in 0 until binding.iceGroup.childCount) {
            val child = binding.iceGroup.getChildAt(i)
            child.isEnabled = !disable
            child.alpha = if (disable) 0.4f else 1f
        }
        if (disable) {
            ice = "No Ice"
            updateIconGroupSelection(binding.iceGroup, "No Ice")
        }
    }

    private fun setupIconGroup(
        container: ViewGroup,
        options: List<Triple<String, Int, Float>>,
        onSelected: (String) -> Unit
    ) {
        container.removeAllViews()
        val defaultSelection = options.first().first

        options.forEach { (label, icon, sizeDp) ->
            val iconView = ImageView(requireContext()).apply {
                setImageResource(icon)
                tag = label
                layoutParams = LinearLayout.LayoutParams(dpToPx(sizeDp).toInt(), dpToPx(sizeDp).toInt()).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setColorFilter(Color.BLACK)
            }

            val labelView = TextView(requireContext()).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(Color.BLACK)
                setPadding(4, 4, 4, 0)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(64f).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
            }

            val containerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = dpToPx(12f).toInt()
                }
                setPadding(16, 16, 16, 16)
                background = ContextCompat.getDrawable(context, R.drawable.chip_unselected_bg)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onSelected(label)
                    updateIconGroupSelection(container, label)
                }
                addView(iconView)
                addView(labelView)
            }

            container.addView(containerLayout)
        }

        onSelected(defaultSelection)
        updateIconGroupSelection(container, defaultSelection)
    }

    private fun updateIconGroupSelection(container: ViewGroup, selected: String) {
        for (i in 0 until container.childCount) {
            val layout = container.getChildAt(i) as LinearLayout
            val icon = layout.getChildAt(0) as ImageView
            val label = layout.getChildAt(1) as TextView

            val isSelected = icon.tag == selected
            val bg = if (isSelected) R.drawable.chip_selected_bg else R.drawable.chip_unselected_bg
            val bgColor = if (isSelected) R.color.redIcon else android.R.color.transparent
            val tint = if (isSelected) Color.WHITE else Color.BLACK

            layout.setBackgroundResource(bg)
            layout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), bgColor)
            icon.setColorFilter(tint)
            label.setTextColor(tint)
        }
    }

    private fun updateTotal() {
        val adjusted = when (size) {
            "Medium" -> basePrice + 1.5
            "Large" -> basePrice + 3.0
            else -> basePrice
        }
        val total = adjusted * quantity
        binding.textTotal.text = "Total: $%.2f".format(total)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.cartPreviewSheet).apply {
            isHideable = true
            skipCollapsed = false
            isDraggable = true
            peekHeight = resources.getDimensionPixelSize(R.dimen.cart_peek_height)

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    animateDimOverlay(newState != BottomSheetBehavior.STATE_HIDDEN)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val safeBinding = _binding ?: return

                    safeBinding.dimOverlay.alpha = slideOffset * 0.7f
                    safeBinding.dimOverlay.visibility = if (slideOffset > 0f) View.VISIBLE else View.GONE
                    safeBinding.btnAddToCart.visibility = if (slideOffset > 0f) View.GONE else View.VISIBLE
                }

            })
        }

        binding.dimOverlay.setOnClickListener {
            hideCartPreview()
        }
    }

    private fun showCartPreview(peek: Boolean = false) {
        bottomSheetBehavior.state =
            if (peek) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideCartPreview() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun animateDimOverlay(show: Boolean) {
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

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
