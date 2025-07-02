package com.example.codecupapp

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun checkAuthenticationOrRedirect() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to access your profile.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.authFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkAuthenticationOrRedirect()

        view.post {
            loadUserInfo()
            setEditListeners()
        }
    }

    private fun loadUserInfo() {
        binding.editUserName.setText(UserData.name)
        binding.editUserEmail.setText(UserData.email)
        binding.editUserPhone.setText(UserData.phone)
        binding.editUserGender.setText(UserData.gender)
        binding.editUserAddress.setText(UserData.address)

        // Disable fields initially
        disableAllFields()
    }

    private fun disableAllFields() {
        listOf(
            binding.editUserName,
            binding.editUserEmail,
            binding.editUserPhone,
            binding.editUserGender,
            binding.editUserAddress
        ).forEach { editText ->
            editText.isEnabled = false
            editText.setBackgroundTintList(null)
        }
    }

    private fun setEditListeners() {
        binding.iconEditName.setOnClickListener {
            enableAndFocus(binding.editUserName) { newValue ->
                UserData.name = newValue
            }
        }

        binding.iconEditEmail.setOnClickListener {
            enableAndFocus(binding.editUserEmail) { newValue ->
                UserData.email = newValue
            }
        }

        binding.iconEditPhone.setOnClickListener {
            enableAndFocus(binding.editUserPhone) { newValue ->
                UserData.phone = newValue
            }
        }

        binding.iconEditGender.setOnClickListener {
            enableAndFocus(binding.editUserGender) { newValue ->
                UserData.gender = newValue
            }
        }

        binding.iconEditAddress.setOnClickListener {
            enableAndFocus(binding.editUserAddress) { newValue ->
                UserData.address = newValue
            }
        }

        binding.btnChangePassword.setOnClickListener {
            showPasswordChangeDialog()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun enableAndFocus(editText: EditText, onSave: (String) -> Unit) {
        editText.isEnabled = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                    editText.isEnabled = false
                } else {
                    Toast.makeText(requireContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPasswordChangeDialog() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "You are not signed in. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }

        val inputOld = EditText(requireContext()).apply {
            hint = "Current Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val inputNew = EditText(requireContext()).apply {
            hint = "New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(inputOld)
        layout.addView(inputNew)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val oldPass = inputOld.text.toString()
                val newPass = inputNew.text.toString()

                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email

                if (user != null && email != null) {
                    val credential = EmailAuthProvider.getCredential(email, oldPass)

                    lifecycleScope.launch(Dispatchers.IO) {
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                user.updatePassword(newPass)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        UserData.points = 0
        UserData.name = "Guest"
        UserData.email = "guest@example.com"
        UserData.phone = ""
        UserData.gender = ""
        UserData.address = ""

        requireActivity().getSharedPreferences("AppPrefs", 0).edit().clear().apply()
        findNavController().navigate(R.id.authFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
