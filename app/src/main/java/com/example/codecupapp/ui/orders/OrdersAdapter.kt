package com.example.codecupapp

import OrderItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.databinding.ItemOrderCardBinding

// Adapter responsible for managing and displaying a list of OrderItem in a RecyclerView
class OrdersAdapter(
    private var orders: List<OrderItem>,
    private val onComplete: (Int) -> Unit  // Callback invoked when an order is long-clicked
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    // ViewHolder class that holds the view binding for each order item
    inner class OrderViewHolder(val binding: ItemOrderCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // UI Workflow: Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOrderCardBinding.inflate(inflater, parent, false)
        return OrderViewHolder(binding)
    }

    // UI Workflow: Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        bindOrderToView(holder, order, position)
    }

    // Data Management: Returns the number of items in the adapter
    override fun getItemCount(): Int = orders.size

    // State Management: Safely retrieves the order at a given position
    fun getItemAt(position: Int): OrderItem? {
        return if (position in orders.indices) orders[position] else null
    }

    // State Management: Updates the adapter’s data and refreshes the view
    fun updateData(newItems: List<OrderItem>) {
        orders = newItems
        notifyDataSetChanged()
    }

    // UI Workflow: Helper function to bind order data to view elements
    private fun bindOrderToView(holder: OrderViewHolder, order: OrderItem, position: Int) {
        with(holder.binding) {
            textOrderTime.text = order.date
            textOrderName.text = order.name
            textOrderAddress.text = order.address
            textOrderPrice.text = "$%.2f".format(order.price)

            // Triggers the callback when the item is long-clicked
            root.setOnLongClickListener {
                onComplete(position)
                true
            }
        }
    }
}
