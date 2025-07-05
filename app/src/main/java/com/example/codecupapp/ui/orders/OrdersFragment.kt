package com.example.codecupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.databinding.FragmentOrdersBinding


class OrdersFragment : Fragment() {

    // View Binding
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModels
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val ordersViewModel: OrdersViewModel by activityViewModels()

    // Adapter for order list
    private lateinit var ordersAdapter: OrdersAdapter

    // Tracks which list is shown
    private var showingOngoing = true

    // Inflate layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Main setup entry point
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOrdersBinding.bind(view)

        setupRecyclerView()
        observeOrderState()
        setupButtons()
        attachSwipeToArchive()

        showOngoing() // Default view
    }

    // Setup RecyclerView and adapter
    private fun setupRecyclerView() {
        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(emptyList()) { position ->
            // Complete order when tapped
            ordersViewModel.completeOrder(position)

            // Optional: Toast if reward threshold met
            if (loyaltyViewModel.stamps.value == 8) {
                Toast.makeText(requireContext(), "You earned a free reward!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerOrders.adapter = ordersAdapter
    }

    // Observe order changes and refresh list when needed
    private fun observeOrderState() {
        ordersViewModel.ongoingOrders.observe(viewLifecycleOwner) {
            if (showingOngoing) showOngoing()
        }

        ordersViewModel.historyOrders.observe(viewLifecycleOwner) {
            if (!showingOngoing) showHistory()
        }
    }

    // Toggle buttons to switch between ongoing and history views
    private fun setupButtons() {
        binding.btnOngoing.setOnClickListener { showOngoing() }
        binding.btnHistory.setOnClickListener { showHistory() }
    }

    // Display ongoing orders
    private fun showOngoing() {
        showingOngoing = true
        val ongoing = ordersViewModel.ongoingOrders.value ?: emptyList()
        ordersAdapter.updateData(ongoing)
    }

    // Display past (history) orders
    private fun showHistory() {
        showingOngoing = false
        val history = ordersViewModel.historyOrders.value ?: emptyList()
        ordersAdapter.updateData(history)
    }

    // Swipe left to archive (move to history)
    private fun attachSwipeToArchive() {
        val swipeToArchive = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val view = viewHolder.itemView

                // If invalid position or wrong list, reset
                if (position == RecyclerView.NO_POSITION || !showingOngoing) {
                    ordersAdapter.notifyItemChanged(position)
                    return
                }

                view.animate()
                    .translationX(-view.width.toFloat())
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        val item = ordersAdapter.getItemAt(position)

                        if (item != null) {
                            ordersViewModel.removeOngoing(item)
                            ordersViewModel.addToHistory(item)
                            ordersAdapter.notifyItemRemoved(position)
                        } else {
                            ordersAdapter.notifyItemChanged(position)
                        }

                        view.alpha = 1f
                        view.translationX = 0f
                    }
                    .start()
            }
        }

        ItemTouchHelper(swipeToArchive).attachToRecyclerView(binding.recyclerOrders)
    }

    // Prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
