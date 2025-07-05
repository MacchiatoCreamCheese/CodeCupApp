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
import com.example.codecupapp.databinding.ItemOrderCardBinding
import java.util.Date
import java.util.Locale



class OrdersAdapter(
    private var orders: List<OrderItem>,
    private val onComplete: (Int) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    // ViewHolder holds reference to each item's binding
    inner class OrderViewHolder(val binding: ItemOrderCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflates layout and creates ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    // Binds data to ViewHolder for each item
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            textOrderTime.text = order.date
            textOrderName.text = order.name
            textOrderAddress.text = order.address
            textOrderPrice.text = "$%.2f".format(order.price)

            // Long click to trigger order completion
            root.setOnLongClickListener {
                onComplete(position)
                true
            }
        }
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = orders.size

    // Utility to safely get item at a specific position
    fun getItemAt(position: Int): OrderItem? {
        return if (position in orders.indices) orders[position] else null
    }

    // Replaces current list with new data and refreshes UI
    fun updateData(newItems: List<OrderItem>) {
        orders = newItems
        notifyDataSetChanged()
    }
}


