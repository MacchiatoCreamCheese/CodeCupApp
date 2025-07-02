package com.example.codecupapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.TopAppBarState
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var isEditing = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textName = view.findViewById<TextView>(R.id.textUserName)
        val inputName = view.findViewById<EditText>(R.id.inputUserName)
        val textEmail = view.findViewById<TextView>(R.id.textUserEmail)
        val inputEmail = view.findViewById<EditText>(R.id.inputUserEmail)
        val btnEdit = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        textName.text = UserData.name
        inputName.setText(UserData.name)
        textEmail.text = UserData.email
        inputEmail.setText(UserData.email)

        btnEdit.setOnClickListener {
            if (!isEditing) {
                // Switch to Edit Mode
                isEditing = true
                btnEdit.text = "Save"

                textName.visibility = View.GONE
                textEmail.visibility = View.GONE
                inputName.visibility = View.VISIBLE
                inputEmail.visibility = View.VISIBLE

            } else {
                // Save changes
                val newName = inputName.text.toString().trim()
                val newEmail = inputEmail.text.toString().trim()

                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    UserData.name = newName
                    UserData.email = newEmail

                    textName.text = newName
                    textEmail.text = newEmail

                    isEditing = false
                    btnEdit.text = "Edit Profile"

                    textName.visibility = View.VISIBLE
                    textEmail.visibility = View.VISIBLE
                    inputName.visibility = View.GONE
                    inputEmail.visibility = View.GONE
                }
            }
        }

        btnLogout.setOnClickListener {
            // Clear User Data
            UserData.points = 0
            UserData.name = "Guest"
            UserData.email = "guest@example.com"

            // Navigate back to Auth screen
            val prefs = requireActivity().getSharedPreferences("AppPrefs", 0)
            prefs.edit().clear().apply()
            findNavController().navigate(R.id.authFragment)
        }
    }
}
