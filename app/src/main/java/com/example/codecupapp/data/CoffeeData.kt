package com.example.codecupapp

data class CoffeeItem(
    val name: String,
    val price: Double,
    val imageResId: Int,
    val points: Int = 0
)

object CoffeeRepository {

    val bestSellers = listOf(
        CoffeeItem("Americano", 3.00, R.drawable.mug_coffee_png16824_1__3_, points = 1),
        CoffeeItem("Mocha", 3.50, R.drawable.mug_coffee_png16824_1__3_, points = 2),
        CoffeeItem("Taco Milktea", 3.75, R.drawable.mug_coffee_png16824_1__3_, points = 4),
        CoffeeItem("Pumpkin Spice", 4.25, R.drawable.mug_coffee_png16824_1__3_, points = 2),
        CoffeeItem("Egg Coffee", 4.00, R.drawable.mug_coffee_png16824_1__3_, points = 1)
    )

    val allCoffees = listOf(
        CoffeeItem("Espresso", 3.00, R.drawable.mug_coffee_png16824_1__3_, points = 3),
        CoffeeItem("Latte", 3.50, R.drawable.mug_coffee_png16824_1__3_, points = 2),
        CoffeeItem("Cappucino", 3.75, R.drawable.mug_coffee_png16824_1__3_, points = 1),
        CoffeeItem("Macchiato", 4.25, R.drawable.mug_coffee_png16824_1__3_, points = 5),
        CoffeeItem("Drip Coffee", 4.00, R.drawable.mug_coffee_png16824_1__3_, points = 3)
    )

    fun getAll(): List<CoffeeItem> = allCoffees + bestSellers

    fun getByName(name: String): CoffeeItem? =
        getAll().find { it.name.equals(name, ignoreCase = true) }
}
