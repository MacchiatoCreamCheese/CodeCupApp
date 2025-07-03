package com.example.codecupapp

import UserData
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private val profileViewModel: ProfileViewModel by viewModels()
    private var isDirty = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        checkAuthenticationOrRedirect()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isDirty) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Unsaved Changes")
                            .setMessage("Do you want to save changes before leaving?")
                            .setPositiveButton("Save") { _, _ ->
                                saveChangesToFirestore()
                                isDirty = false
                                findNavController().popBackStack()
                            }
                            .setNegativeButton("Discard") { _, _ ->
                                isDirty = false
                                findNavController().popBackStack()
                            }
                            .show()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        )

        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.editUserName.setText(profile.name)
            binding.editUserEmail.setText(profile.email)
            binding.editUserPhone.setText(profile.phone)
            binding.editUserGender.setText(profile.gender)
            binding.editUserAddress.setText(profile.address)
            disableAllFields()
        }

        profileViewModel.loadProfile()
        setEditListeners()
    }

    private fun checkAuthenticationOrRedirect() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to access your profile.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.authFragment)
        }
    }


    private fun disableAllFields() {
        listOf(
            binding.editUserName,
            binding.editUserEmail,
            binding.editUserPhone,
            binding.editUserGender,
            binding.editUserAddress
        ).forEach {
            it.isEnabled = false
            it.setBackgroundTintList(null)
        }
    }

    private fun setEditListeners() {
        binding.iconEditName.setOnClickListener {
            enableAndFocus(binding.editUserName) {
                isDirty = true
                saveChangesToFirestore()
            }
        }

        binding.iconEditEmail.setOnClickListener {
            enableAndFocus(binding.editUserEmail) {
                isDirty = true
                saveChangesToFirestore()
            }
        }

        binding.iconEditPhone.setOnClickListener {
            enableAndFocus(binding.editUserPhone) {
                isDirty = true
                saveChangesToFirestore()
            }
        }

        binding.iconEditGender.setOnClickListener {
            enableAndFocus(binding.editUserGender) {
                isDirty = true
                saveChangesToFirestore()
            }
        }

        binding.iconEditAddress.setOnClickListener {
            enableAndFocus(binding.editUserAddress) {
                isDirty = true
                saveChangesToFirestore()
            }
        }

        binding.btnChangePassword.setOnClickListener {
            showPasswordChangeDialog()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun enableAndFocus(editText: EditText, onEdit: () -> Unit) {
        editText.isEnabled = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                onEdit()
                editText.isEnabled = false
            }
        }
    }

    private fun saveChangesToFirestore() {
        val updated = UserData(
            name = binding.editUserName.text.toString().trim(),
            email = binding.editUserEmail.text.toString().trim(),
            phone = binding.editUserPhone.text.toString().trim(),
            gender = binding.editUserGender.text.toString().trim(),
            address = binding.editUserAddress.text.toString().trim()
        )

        profileViewModel.updateProfile(
            updated = updated,
            onSuccess = {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        )

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

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val oldPass = inputOld.text.toString()
                val newPass = inputNew.text.toString()

                val email = user.email
                if (email != null) {
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
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.authFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
