package com.example.codecupapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CartItem

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textCartItemName)
        val options: TextView = view.findViewById(R.id.textCartItemOptions)
        val price: TextView = view.findViewById(R.id.textCartItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.options.text = "${item.shot} | ${item.temperature} | ${item.size} | ${item.ice}"
        holder.price.text = "$%.2f x${item.quantity}".format(item.unitPrice)

    }

    override fun getItemCount(): Int = items.size


    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }


}
