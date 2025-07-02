package com.example.codecupapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.data.CoffeePointsConfig
import com.example.codecupapp.data.OrderItem
import com.example.codecupapp.databinding.FragmentCartBinding

class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private val ordersViewModel: OrdersViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCartBinding.bind(view)

        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())

        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            binding.recyclerCart.adapter = CartAdapter(items.toMutableList()) { position ->
                cartViewModel.dispatch(CartAction.RemoveItem(items[position]))
            }
            updateTotal(items)
            (requireActivity() as MainActivity).updateCartBadge(items.sumOf { it.quantity })
        }


        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CartAdapter(
            cartViewModel.cartItems.value.orEmpty().toMutableList()
        ) { position ->
            // Remove item from ViewModel
            val item = cartViewModel.cartItems.value?.getOrNull(position)
            if (item != null) {
                cartViewModel.dispatch(CartAction.RemoveItem(item))
            }
        }
        binding.recyclerCart.adapter = adapter

        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())

// Swipe to delete
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val view = viewHolder.itemView

                view.animate()
                    .translationX(-view.width.toFloat())
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        adapter.removeAt(position)
                        val item = cartViewModel.cartItems.value?.getOrNull(position)
                        if (item != null) {
                            cartViewModel.dispatch(CartAction.RemoveItem(item))
                        }
                    }
                    .start()
            }

        }

        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.recyclerCart)


        binding.btnCheckout.setOnClickListener {

            val cartItems = cartViewModel.cartItems.value.orEmpty()

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPrice = getTotalPrice(cartItems)
            var totalPoints = 0

            cartViewModel.cartItems.value?.forEach { item ->

                val itemName = item.name.lowercase()

                val basePoints = CoffeePointsConfig.getPointsForCoffee(itemName)
                val earnedPoints = basePoints * item.quantity

                totalPoints += earnedPoints
            }



            val formatter = java.text.SimpleDateFormat("dd MMMM | h:mm a", java.util.Locale.getDefault())
            val currentDateTime = formatter.format(java.util.Date())
            val selectedType = ordersViewModel.deliveryType.value ?: "Deliver"
            val address = if (selectedType == "Deliver") "User Address" else "Pickup at Code Cup"

            val nameFormatter = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
            val nameTime = nameFormatter.format(java.util.Date())

// Generate a unique name like: Order_20250702_1405_DELIVER
            val uniqueName = "Order_${nameTime}_${selectedType.uppercase()}"

            rewardsViewModel.addPoints(uniqueName, totalPoints)

            ordersViewModel.addOngoing(
                OrderItem(
                    date = currentDateTime,
                    name = uniqueName,
                    address = address,
                    price = totalPrice
                )
            )

            cartViewModel.dispatch(CartAction.ClearCart)
            findNavController().navigate(R.id.orderSuccessFragment)
        }
    }

    private fun updateTotal(items: List<CartItem>) {
        val total = getTotalPrice(items)
        binding.textTotalPrice.text = "Total: $%.2f".format(total)
    }

    private fun getTotalPrice(items: List<CartItem>): Double {
        return items.sumOf { it.unitPrice * it.quantity }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

