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

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private val ordersViewModel: OrdersViewModel by activityViewModels()

    private lateinit var ordersAdapter: OrdersAdapter


    private var showingOngoing = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOrdersBinding.bind(view)

        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(emptyList()) { position ->
            ordersViewModel.completeOrder(position)

            if (loyaltyViewModel.stamps.value == 8) {
                Toast.makeText(requireContext(), "You earned a free reward!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerOrders.adapter = ordersAdapter

        // Attach swipe helper
        attachSwipeToArchive()


        binding.btnOngoing.setOnClickListener { showOngoing() }
        binding.btnHistory.setOnClickListener { showHistory() }

        ordersViewModel.ongoingOrders.observe(viewLifecycleOwner) {
            if (showingOngoing) showOngoing()
        }

        ordersViewModel.historyOrders.observe(viewLifecycleOwner) {
            if (!showingOngoing) showHistory()
        }

        showOngoing()

    }


    private fun showOngoing() {
        showingOngoing = true
        ordersAdapter.updateData(ordersViewModel.ongoingOrders.value ?: emptyList())
    }

    private fun showHistory() {
        showingOngoing = false
        ordersAdapter.updateData(ordersViewModel.historyOrders.value ?: emptyList())
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
