package com.example.codecupapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.codecupapp.data.SharedPrefsManager
import com.example.codecupapp.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
class AuthFragment : Fragment() {

    // View binding reference (state management)
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    // State: tracks whether we are in login mode
    private var isLoginMode = false

    // Firebase authentication instance (state management)
    private lateinit var auth: FirebaseAuth

    // UI lifecycle: initializes view binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    // UI lifecycle: setup logic after view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AuthFragment", "AuthFragment started")

        auth = FirebaseAuth.getInstance()

        // Auth check: skip auth if user is already logged in
        auth.currentUser?.let {
            navigateToHome()
            return
        }

        setupButtonListeners()
    }

    // UI logic: Sets up button click listeners
    private fun setupButtonListeners() {
        binding.btnSignInMode.setOnClickListener {
            if (isLoginMode) switchToSignIn() else performSignIn()
        }

        binding.btnLoginMode.setOnClickListener {
            if (!isLoginMode) switchToLogin() else performLogin()
        }
    }

    // UI logic: Switch to login view
    private fun switchToLogin() {
        isLoginMode = true
        binding.textTitle.text = "Log in"
        setExtraFieldsVisibility(View.GONE)
    }

    // UI logic: Switch to sign-in view
    private fun switchToSignIn() {
        isLoginMode = false
        binding.textTitle.text = "Sign in"
        setExtraFieldsVisibility(View.VISIBLE)
    }

    // UI logic: Toggle visibility of fields that only appear during sign-up
    private fun setExtraFieldsVisibility(visibility: Int) {
        binding.inputName.visibility = visibility
        binding.inputPhone.visibility = visibility
        binding.inputGender.visibility = visibility
        binding.inputAddress.visibility = visibility
    }

    // Authentication: Handles sign-up logic
    private fun performSignIn() {
        val name = binding.inputName.text.toString().trim()
        val phone = binding.inputPhone.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        val gender = binding.inputGender.text.toString().trim()
        val address = binding.inputAddress.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        // Validation
        if (listOf(name, phone, email, gender, address, password).any { it.isEmpty() }) {
            showToast("Please fill all fields")
            return
        }

        // Firebase account creation
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.let { user ->
                    saveUserProfileToFirestore(user.uid, name, phone, email, gender, address)
                }
            }
            .addOnFailureListener {
                showToast("Error: ${it.message}")
            }
    }

    // Firestore: Save new user profile data
    private fun saveUserProfileToFirestore(
        uid: String,
        name: String,
        phone: String,
        email: String,
        gender: String,
        address: String
    ) {
        val profile = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "gender" to gender,
            "address" to address
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(profile)
            .addOnSuccessListener {
                showToast("Account created!")
                loadAndSaveUserProfile()
            }
            .addOnFailureListener {
                showToast("Failed to save profile: ${it.message}")
            }
    }

    // Authentication: Handles login logic
    private fun performLogin() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Email and password required")
            return
        }

        // Firebase login
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                loadAndSaveUserProfile()
            }
            .addOnFailureListener {
                showToast("Login failed: ${it.message}")
            }
    }

    // Workflow: Load user profile from Firestore and save it locally
    private fun loadAndSaveUserProfile() {
        lifecycleScope.launch {
            try {
                val userData = ProfileRepository.loadUserProfileSuspend(requireContext())
                SharedPrefsManager.saveUserProfile(requireContext(), userData)
                navigateToHome()
            } catch (e: Exception) {
                showToast("Error loading profile: ${e.message}")
            }
        }
    }

    // Navigation: Directs user to the home screen
    private fun navigateToHome() {
        findNavController().navigate(R.id.homeFragment)
    }

    // Utility: Reusable toast function
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Lifecycle: Clean up view binding reference to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
