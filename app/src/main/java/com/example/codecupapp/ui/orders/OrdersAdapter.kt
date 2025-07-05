package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import OrderItem
import com.example.codecupapp.databinding.ItemOrderCardBinding

class OrdersAdapter(
    private var orders: List<OrderItem>,
    private val onComplete: (Int) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            textOrderTime.text = order.date
            textOrderName.text = order.name
            textOrderAddress.text = order.address
            textOrderPrice.text = "$%.2f".format(order.price)

            root.setOnLongClickListener {
                onComplete(position)
                true
            }
        }
    }

    override fun getItemCount(): Int = orders.size

    fun getItemAt(position: Int): OrderItem? {
        return if (position in orders.indices) orders[position] else null
    }

    fun updateData(newItems: List<OrderItem>) {
        orders = newItems
        notifyDataSetChanged()
    }

}
