import com.example.codecupapp.RedeemItem
import com.example.codecupapp.data.OrderItem

data class UserData(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var gender: String = "",
    var address: String = "",
    var points: Int = 0,
    var ongoingOrders: List<OrderItem> = listOf(),   // ✅ New field
    var historyOrders: List<OrderItem> = listOf(),   // ✅ New field
    var redeemHistory: List<RedeemItem> = listOf()  // ✅ Optional, if using
)



