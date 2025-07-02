package com.example.codecupapp.data

import android.util.Log

object CoffeePointsConfig {

    private val pointsMap = mutableMapOf<String, Int>()

    fun initializeDefaults() {
        pointsMap["americano"] = 1
        pointsMap["mocha"] = 2
        pointsMap["macchiato"] = 5
        pointsMap["latte"] = 2
        pointsMap["espresso"] = 3

        Log.d("PointsDebug", "Initialized Points Map: $pointsMap")
    }

    fun setPointsForCoffee(name: String, points: Int) {
        pointsMap[name.lowercase()] = points
        Log.d("PointsDebug", "Updated Points for ${name.lowercase()}: $points")
    }

    fun getPointsForCoffee(name: String): Int {
        val points = pointsMap[name.lowercase()] ?: 0
        Log.d("PointsDebug", "Fetching Points for ${name.lowercase()}: $points")
        return points
    }
}
