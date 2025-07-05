package com.example.codecupapp

import OrderItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.FragmentCartBinding
import java.util.Date
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private val ordersViewModel: OrdersViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeCartItems()
        handleCheckout()
    }

    /** 🧃 Setup RecyclerView and adapter */
    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(mutableListOf()) { position ->
            val item = cartAdapter.getItemAt(position)
            if (item != null) {
                cartViewModel.dispatch(CartAction.RemoveItem(item))
            }
        }

        binding.recyclerCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    /** 🔁 Observe cart ViewModel and update UI accordingly */
    private fun observeCartItems() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.updateData(items.toMutableList())
            updateTotal(items)
            (requireActivity() as? MainActivity)?.updateCartBadge(items.sumOf { it.quantity })
        }
    }

    /** 🧼 Setup swipe-to-delete with proper item binding */
    private fun setupSwipeToDelete() {
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val view = viewHolder.itemView

                if (position == RecyclerView.NO_POSITION) {
                    cartAdapter.notifyItemChanged(position)
                    return
                }

                view.animate()
                    .translationX(-view.width.toFloat())
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        val item = cartAdapter.getItemAt(position)
                        if (item != null) {
                            cartViewModel.dispatch(CartAction.RemoveItem(item))
                            cartAdapter.removeAt(position)
                        } else {
                            cartAdapter.notifyItemChanged(position)
                        }

                        view.alpha = 1f
                        view.translationX = 0f
                    }
                    .start()
            }
        }

        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.recyclerCart)
    }

    /** ✅ Checkout button behavior */
    private fun handleCheckout() {
        binding.btnCheckout.setOnClickListener {
            val cartItems = cartViewModel.cartItems.value.orEmpty()

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPrice = getTotalPrice(cartItems)
            var totalPoints = 0

            cartItems.forEach { item ->
                val coffee = CoffeeRepository.getByName(item.name)
                val basePoints = coffee?.points ?: 0
                totalPoints += basePoints * item.quantity
            }

            val formatter = java.text.SimpleDateFormat("dd MMMM | h:mm a", Locale.getDefault())
            val currentDateTime = formatter.format(Date())
            val selectedType = ordersViewModel.deliveryType.value ?: "Deliver"
            val address = if (selectedType == "Deliver") "User Address" else "Pickup at Code Cup"

            val nameFormatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val nameTime = nameFormatter.format(Date())
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

