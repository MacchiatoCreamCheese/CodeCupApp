package com.example.codecupapp

import UserData
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.google.firebase.firestore.FirebaseFirestore
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

        checkAuthenticationOrRedirect()
        profileViewModel.loadFromLocal(requireContext())
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

        val key = when (editText) {
            binding.editUserName -> "name"
            binding.editUserPhone -> "phone"
            binding.editUserGender -> "gender"
            binding.editUserAddress -> "address"
            else -> null
        }

        key?.let { cacheProfileField(it, editText.text.toString().trim()) }

        currentlyEditingField = null
        currentEditIcon = null
        isDirty = false
    }

    private fun promptToSaveOrDiscard(onDiscard: () -> Unit) {
        val editText = currentlyEditingField ?: return

        val key = when (editText) {
            binding.editUserName -> "name"
            binding.editUserPhone -> "phone"
            binding.editUserGender -> "gender"
            binding.editUserAddress -> "address"
            else -> null
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("Save or discard changes?")
            .setPositiveButton("Save") { _, _ ->
                key?.let {
                    cacheProfileField(it, editText.text.toString().trim())
                }
                saveAndExitEdit(editText)
            }
            .setNegativeButton("Discard") { _, _ -> onDiscard() }
            .show()
    }

    private fun cacheProfileField(key: String, value: String) {
        SharedPrefsManager.updateField(requireContext(), key, value)
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
        val updates = SharedPrefsManager.getAllAsMap(requireContext())
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null && updates.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile synced", Toast.LENGTH_SHORT).show()
                    SharedPrefsManager.clear(requireContext())
                    finishLogout()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to sync: ${it.message}", Toast.LENGTH_SHORT).show()
                    finishLogout()
                }
        } else {
            finishLogout()
        }
    }



    private fun finishLogout() {
        FirebaseAuth.getInstance().signOut()
        SharedPrefsManager.clear(requireContext()) // if you’re using this
        findNavController().navigate(R.id.authFragment)
    }


    override fun onDestroyView() {
         _binding = null
        super.onDestroyView()
    }

}
