package com.example.codecupapp.ui.auth

import CCAccount
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.R
import com.google.android.material.button.MaterialButton



class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var isLoginMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val textTitle = view.findViewById<TextView>(R.id.textTitle)
        val inputName = view.findViewById<EditText>(R.id.inputName)
        val inputPhone = view.findViewById<EditText>(R.id.inputPhone)
        val inputEmail = view.findViewById<EditText>(R.id.inputEmail)
        val inputGender = view.findViewById<EditText>(R.id.inputGender)
        val inputAddress = view.findViewById<EditText>(R.id.inputAddress)
        val inputPassword = view.findViewById<EditText>(R.id.inputPassword)
        val btnSignInMode = view.findViewById<MaterialButton>(R.id.btnSignInMode)
        val btnLoginMode = view.findViewById<MaterialButton>(R.id.btnLoginMode)

        btnSignInMode.setOnClickListener {
            if (isLoginMode) {
                switchToSignIn(textTitle, inputName, inputPhone, inputGender, inputAddress)
            } else {
                performSignIn(inputName, inputPhone, inputEmail, inputGender, inputAddress, inputPassword)
            }
        }

        btnLoginMode.setOnClickListener {
            if (!isLoginMode) {
                switchToLogin(textTitle, inputName, inputPhone, inputGender, inputAddress)
            } else {
                performLogin(inputPhone, inputPassword)
            }
        }
    }

    private fun switchToLogin(title: TextView, name: EditText, phone: EditText, gender: EditText, address: EditText) {
        isLoginMode = true
        title.text = "Log in"
        name.visibility = View.GONE
        gender.visibility = View.GONE
        address.visibility = View.GONE
    }

    private fun switchToSignIn(title: TextView, name: EditText, phone: EditText, gender: EditText, address: EditText) {
        isLoginMode = false
        title.text = "Sign in"
        name.visibility = View.VISIBLE
        gender.visibility = View.VISIBLE
        address.visibility = View.VISIBLE
    }

    private fun performSignIn(name: EditText, phone: EditText, email: EditText, gender: EditText, address: EditText, password: EditText) {
        val nameVal = name.text.toString().trim()
        val phoneVal = phone.text.toString().trim()
        val emailVal = email.text.toString().trim()
        val genderVal = gender.text.toString().trim()
        val addressVal = address.text.toString().trim()
        val passwordVal = password.text.toString().trim()

        if (listOf(nameVal, phoneVal, emailVal, genderVal, addressVal, passwordVal).any { it.isEmpty() }) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (UserData.accounts.any { it.phone == phoneVal }) {
            Toast.makeText(requireContext(), "Phone already registered", Toast.LENGTH_SHORT).show()
            return
        }
        UserData.accounts.add(CCAccount(nameVal, phoneVal, emailVal, genderVal, addressVal, passwordVal))
        Toast.makeText(requireContext(), "Account created! Please Log in.", Toast.LENGTH_SHORT).show()
        switchToLogin(requireView().findViewById(R.id.textTitle), name, phone, gender, address)
    }

    private fun performLogin(phone: EditText, password: EditText) {
        val phoneVal = phone.text.toString().trim()
        val passwordVal = password.text.toString().trim()

        val user = UserData.accounts.find { it.phone == phoneVal && it.password == passwordVal }
        if (user == null) {
            Toast.makeText(requireContext(), "Incorrect credentials", Toast.LENGTH_SHORT).show()
            return
        }

        UserData.name = user.name
        UserData.phone = user.phone
        UserData.email = user.email
        UserData.gender = user.gender
        UserData.address = user.address

        val prefs = requireActivity().getSharedPreferences("AppPrefs", 0)
        prefs.edit().apply {
            putString("username", UserData.name)
            putString("email", UserData.email)
            putBoolean("logged_in", true)
            apply()
        }
        findNavController().navigate(R.id.homeFragment)
    }
}
