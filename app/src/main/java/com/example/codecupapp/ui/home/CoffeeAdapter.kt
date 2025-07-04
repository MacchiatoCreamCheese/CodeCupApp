package com.example.codecupapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CoffeeItem

class CoffeeAdapter(
    private val coffeeList: List<CoffeeItem>,
    private val onItemClick: (CoffeeItem) -> Unit
) : RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder>() {

    inner class CoffeeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coffeeName: TextView = view.findViewById(R.id.textCoffeeName)
        val coffeePrice: TextView = view.findViewById(R.id.textCoffeePrice)
        val coffeeImage: ImageView = view.findViewById(R.id.imageCoffee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coffee_card, parent, false)
        return CoffeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        val coffee = coffeeList[position]
        holder.coffeeName.text = coffee.name
        holder.coffeePrice.text = "$%.2f".format(coffee.price)
        holder.coffeeImage.setImageResource(coffee.imageResId)

        holder.itemView.setOnClickListener {
            onItemClick(coffee)
        }
    }

    override fun getItemCount(): Int = coffeeList.size
}
