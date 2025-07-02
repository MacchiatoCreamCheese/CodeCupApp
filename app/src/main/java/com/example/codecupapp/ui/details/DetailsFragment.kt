package com.example.codecupapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.databinding.FragmentDetailsBinding
import com.example.codecupapp.data.CartItem

class DetailsFragment : Fragment(R.layout.fragment_details) {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    private var quantity = 1
    private var shot = "Single"
    private var temperature = "Hot"
    private var size = "Medium"
    private var ice = "Full Ice"
    private var basePrice = 3.00

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentDetailsBinding.bind(view)

        val coffeeName = arguments?.getString("coffeeName") ?: "Coffee"
        binding.textCoffeeTitle.text = coffeeName
        binding.textQuantity.text = quantity.toString()

        setupOptionGroup(binding.shotGroup, listOf("Single", "Double")) { selected ->
            shot = selected
        }
        setupOptionGroup(binding.tempGroup, listOf("Hot", "Iced")) { selected ->
            temperature = selected
        }
        setupOptionGroup(binding.sizeGroup, listOf("Small", "Medium", "Large")) { selected ->
            size = selected
        }
        setupOptionGroup(binding.iceGroup, listOf("No Ice", "Some Ice", "Full Ice")) { selected ->
            ice = selected
        }

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

        binding.btnAddToCart.setOnClickListener {
            cartViewModel.dispatch(
                CartAction.AddItem(
                    CartItem(
                        name = coffeeName,
                        shot = shot,
                        temperature = temperature,
                        size = size,
                        ice = ice,
                        quantity = quantity,
                        unitPrice = basePrice
                    )
                )
            )
            findNavController().navigate(R.id.cartFragment)
        }

        updateTotal()
    }

    private fun setupOptionGroup(container: LinearLayout, options: List<String>, onSelected: (String) -> Unit) {
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

    private fun updateSelection(container: LinearLayout, selected: String) {
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
