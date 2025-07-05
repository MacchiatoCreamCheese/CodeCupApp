import androidx.annotation.Keep

@Keep
data class OrderItem(
    var date: String = "",
    var name: String = "",
    var address: String = "",
    var price: Double = 0.0
)

@Keep
data class PointTransaction(
    val source: String,  // "Order: Latte" or "Redeemed Free Coffee"
    val amount: Int,     // Positive = Earned, Negative = Spent
    val date: String
)

@Keep
data class UserData(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var gender: String = "",
    var address: String = "",
    var points: Int = 0,
    var stamps: Int = 0,
    var ongoingOrders: List<OrderItem> = listOf(),
    var historyOrders: List<OrderItem> = listOf(),
    var redeemHistory: List<PointTransaction> = listOf()
)

