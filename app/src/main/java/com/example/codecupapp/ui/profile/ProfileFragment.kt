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

    // View Binding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel for user profile
    private val profileViewModel: ProfileViewModel by viewModels()

    // Tracks unsaved state
    private var isDirty = false

    // Tracks currently editing field & its icon
    private var currentlyEditingField: EditText? = null
    private var currentEditIcon: View? = null

    // Inflate layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Setup lifecycle logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        profileViewModel.loadFromLocal(requireContext())


        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.textUserEmail.text = currentUser.email
        } else {
            binding.textUserEmail.text = "No user signed in"
        }

        checkAuthenticationOrRedirect()

        handleBackNavigation()

        observeUserProfile()


        setEditListeners()
    }

    // If user is not logged in, redirect to auth screen
    private fun checkAuthenticationOrRedirect() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to access your profile.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.authFragment)
        }
    }

    // Observes profile LiveData and updates fields.
    private fun observeUserProfile() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.editUserName.setText(profile.name)
            binding.editUserPhone.setText(profile.phone)
            binding.editUserGender.setText(profile.gender)
            binding.editUserAddress.setText(profile.address)
            disableAllFields()
        }
    }

    //Intercept system back button for unsaved changes.
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
            }
        )
    }

    // Disable all input fields until editing is triggered.
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

    // Setup individual edit icon click listeners.
    private fun setEditListeners() {
        setToggleEdit(binding.editUserName, binding.iconEditName)
        setToggleEdit(binding.editUserPhone, binding.iconEditPhone)
        setToggleEdit(binding.editUserGender, binding.iconEditGender)
        setToggleEdit(binding.editUserAddress, binding.iconEditAddress)

        binding.btnChangePassword.setOnClickListener { showPasswordChangeDialog() }
        binding.btnLogout.setOnClickListener { logoutUser() }
    }

    // Handle edit icon.
    private fun setToggleEdit(editText: EditText, icon: View) {
        icon.setOnClickListener {
            when {
                currentlyEditingField == null -> {
                    editText.isEnabled = true
                    editText.requestFocus()
                    editText.setSelection(editText.text.length)
                    currentlyEditingField = editText
                    currentEditIcon = icon
                    isDirty = true
                    //animateBorder(editText, true)
                }

                currentlyEditingField == editText -> {
                    editText.isEnabled = false
                    saveChangesToFirestore()
                    //animateBorder(editText, false)
                    currentlyEditingField = null
                    currentEditIcon = null
                    isDirty = false
                }

                else -> {
                    promptToSaveOrDiscard {
                        currentlyEditingField?.isEnabled = false
                        //currentlyEditingField?.let { animateBorder(it, false) }

                        editText.isEnabled = true
                        editText.requestFocus()
                        editText.setSelection(editText.text.length)
                        currentlyEditingField = editText
                        currentEditIcon = icon
                        isDirty = true
                        //animateBorder(editText, true)
                    }
                }
            }
        }

    }

    //Alert dialog: Save or Discard unsaved data.
    private fun promptToSaveOrDiscard(onDiscard: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Save or discard?")
            .setPositiveButton("Save") { _, _ ->
                saveChangesToFirestore()
                isDirty = false
                currentlyEditingField?.isEnabled = false
                currentlyEditingField = null
                currentEditIcon = null
            }
            .setNegativeButton("Discard") { _, _ ->
                onDiscard()
            }
            .show()
    }

    //Send updated profile data to Firestore.
    private fun saveChangesToFirestore() {
        val updated = UserData(
            name = binding.editUserName.text.toString().trim(),
            phone = binding.editUserPhone.text.toString().trim(),
            gender = binding.editUserGender.text.toString().trim(),
            address = binding.editUserAddress.text.toString().trim()
        )

        ProfileRepository.updateUserProfile(
            requireContext(),
            updated = updated,
            onSuccess = {
                if (isAdded && context != null) {
                    profileViewModel.setProfile(updated)
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Show password change dialog with re-auth.
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

    // Logout and redirect to auth screen.

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        SharedPrefsManager.clear(requireContext())
        findNavController().navigate(R.id.authFragment)
    }


    /**
     * 🧼 Clear view binding to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
