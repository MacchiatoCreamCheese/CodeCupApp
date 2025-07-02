package com.example.codecupapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.data.CartItem

class DetailsFragment : Fragment(R.layout.fragment_details) {
    private val cartViewModel: CartViewModel by activityViewModels()

    private var quantity = 1
    private var shot = "Single"
    private var temperature = "Hot"
    private var size = "Medium"
    private var ice = "Full Ice"
    private var basePrice = 3.00

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val coffeeName = arguments?.getString("coffeeName") ?: "Coffee"

        val title = view.findViewById<TextView>(R.id.textCoffeeTitle)
        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val textTotal = view.findViewById<TextView>(R.id.textTotal)
        val btnAddToCart = view.findViewById<Button>(R.id.btnAddToCart)
        val textQuantity = view.findViewById<TextView>(R.id.textQuantity)

        btnPlus.setOnClickListener {
            quantity++
            textQuantity.text = quantity.toString()
        }
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                textQuantity.text = quantity.toString()
            }
        }

        title.text = coffeeName

        setupOptionGroup(
            view.findViewById(R.id.shotGroup),
            listOf("Single", "Double"),
        ) { selected ->
            shot = selected
        }

        setupOptionGroup(
            view.findViewById(R.id.tempGroup),
            listOf("Hot", "Iced"),
        ) { selected ->
            temperature = selected
        }

        setupOptionGroup(
            view.findViewById(R.id.sizeGroup),
            listOf("Small", "Medium", "Large"),
        ) { selected ->
            size = selected
        }

        setupOptionGroup(
            view.findViewById(R.id.iceGroup),
            listOf("No Ice", "Some Ice", "Full Ice"),
        ) { selected ->
            ice = selected
        }



        btnAddToCart.setOnClickListener {
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


        updateTotal(textTotal)
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
        // Default selection
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

    private fun updateTotal(textView: TextView) {
        val total = basePrice * quantity
        textView.text = "Total: $%.2f".format(total)
    }
}


