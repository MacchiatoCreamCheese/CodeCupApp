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
import com.example.codecupapp.data.SharedPrefsManager
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
    private var currentlyEditingField: EditText? = null
    private var currentEditIcon: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.loadFromLocal(requireContext())
        checkAuthenticationOrRedirect()
        observeUserProfile()
        handleBackNavigation()
        setupEditListeners()

        FirebaseAuth.getInstance().currentUser?.let {
            binding.textUserEmail.text = it.email
        } ?: run {
            binding.textUserEmail.text = "No user signed in"
        }
    }

    // Redirect if not logged in
    private fun checkAuthenticationOrRedirect() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.authFragment)
        }
    }

    // Bind ViewModel to form
    private fun observeUserProfile() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.editUserName.setText(profile.name)
            binding.editUserPhone.setText(profile.phone)
            binding.editUserGender.setText(profile.gender)
            binding.editUserAddress.setText(profile.address)
            disableAllFields()
        }
    }

    // Disable edits by default
    private fun disableAllFields() {
        listOf(
            binding.editUserName,
            binding.editUserPhone,
            binding.editUserGender,
            binding.editUserAddress
        ).forEach {
            it.isEnabled = false
            it.setBackgroundTintList(null)
        }
    }

    // Enable editing one field at a time
    private fun setupEditListeners() {
        setToggleEdit(binding.editUserName, binding.iconEditName)
        setToggleEdit(binding.editUserPhone, binding.iconEditPhone)
        setToggleEdit(binding.editUserGender, binding.iconEditGender)
        setToggleEdit(binding.editUserAddress, binding.iconEditAddress)

        binding.btnChangePassword.setOnClickListener { showPasswordChangeDialog() }
        binding.btnLogout.setOnClickListener { logoutUser() }
    }

    private fun setToggleEdit(editText: EditText, icon: View) {
        icon.setOnClickListener {
            when {
                currentlyEditingField == null -> startEditing(editText, icon)
                currentlyEditingField == editText -> saveAndExitEdit(editText)
                else -> promptToSaveOrDiscard {
                    currentlyEditingField?.isEnabled = false
                    startEditing(editText, icon)
                }
            }
        }
    }

    private fun startEditing(editText: EditText, icon: View) {
        editText.isEnabled = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        currentlyEditingField = editText
        currentEditIcon = icon
        isDirty = true
    }

    private fun saveAndExitEdit(editText: EditText) {
        editText.isEnabled = false
        saveChangesToFirestore()
        currentlyEditingField = null
        currentEditIcon = null
        isDirty = false
    }

    private fun promptToSaveOrDiscard(onDiscard: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("Save or discard changes?")
            .setPositiveButton("Save") { _, _ ->
                saveChangesToFirestore()
                saveAndExitEdit(currentlyEditingField!!)
            }
            .setNegativeButton("Discard") { _, _ -> onDiscard() }
            .show()
    }

    private fun saveChangesToFirestore() {
        val updated = UserData(
            name = binding.editUserName.text.toString().trim(),
            phone = binding.editUserPhone.text.toString().trim(),
            gender = binding.editUserGender.text.toString().trim(),
            address = binding.editUserAddress.text.toString().trim()
        )

        ProfileRepository.updateUserProfile(
            context = requireContext(),
            updated = updated,
            onSuccess = {
                profileViewModel.setProfile(updated)
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Ask to save before navigating back
    private fun handleBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isDirty && currentlyEditingField != null) {
                        promptToSaveOrDiscard {
                            findNavController().popBackStack()
                        }
                    } else {
                        findNavController().popBackStack()
                    }
                }
            })
    }

    // Show password change dialog
    private fun showPasswordChangeDialog() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show()
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
                val email = user.email ?: return@setPositiveButton

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
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Sign out and clear saved data
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        SharedPrefsManager.clear(requireContext())
        findNavController().navigate(R.id.authFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
