object UserData {
    var name = ""
    var phone = ""
    var email = ""
    var gender = ""
    var address = ""
    var points = 0

    // Predefined test account for easy login
    val accounts = mutableListOf(
        CCAccount(
            name = "Nguyen Thu Uyen",
            phone = "0338010512",
            email = "uyen@example.com",
            gender = "Female",
            address = "123 Red Avenue, Texas",
            password = "Female"
        )
    )
}

data class CCAccount(
    val name: String,
    val phone: String,
    val email: String,
    val gender: String,
    val address: String,
    val password: String
)
