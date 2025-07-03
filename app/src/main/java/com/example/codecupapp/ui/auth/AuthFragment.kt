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
import com.example.codecupapp.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private var isLoginMode = false
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AuthFragment", "AuthFragment started")

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Optional: guard again, though splash should skip this already
            findNavController().navigate(R.id.homeFragment)
            return
        }



        binding.btnSignInMode.setOnClickListener {
            if (isLoginMode) {
                switchToSignIn()
            } else {
                performSignIn()
            }
        }

        binding.btnLoginMode.setOnClickListener {
            if (!isLoginMode) {
                switchToLogin()
            } else {
                performLogin()
            }
        }
    }

    private fun switchToLogin() {
        isLoginMode = true
        binding.textTitle.text = "Log in"
        binding.inputName.visibility = View.GONE
        binding.inputPhone.visibility = View.GONE
        binding.inputGender.visibility = View.GONE
        binding.inputAddress.visibility = View.GONE
    }

    private fun switchToSignIn() {
        isLoginMode = false
        binding.textTitle.text = "Sign in"
        binding.inputName.visibility = View.VISIBLE
        binding.inputPhone.visibility = View.VISIBLE
        binding.inputGender.visibility = View.VISIBLE
        binding.inputAddress.visibility = View.VISIBLE
    }

    private fun performSignIn() {
        val nameVal = binding.inputName.text.toString().trim()
        val phoneVal = binding.inputPhone.text.toString().trim()
        val emailVal = binding.inputEmail.text.toString().trim()
        val genderVal = binding.inputGender.text.toString().trim()
        val addressVal = binding.inputAddress.text.toString().trim()
        val passwordVal = binding.inputPassword.text.toString().trim()

        if (listOf(nameVal, phoneVal, emailVal, genderVal, addressVal, passwordVal).any { it.isEmpty() }) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(emailVal, passwordVal)
            .addOnSuccessListener {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val uid = user.uid
                    val profile = mapOf(
                        "name" to nameVal,
                        "email" to emailVal,
                        "phone" to phoneVal,
                        "gender" to genderVal,
                        "address" to addressVal
                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(profile)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Account created!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.homeFragment)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to save profile: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performLogin() {
        val emailVal = binding.inputEmail.text.toString().trim()
        val passwordVal = binding.inputPassword.text.toString().trim()

        if (emailVal.isEmpty() || passwordVal.isEmpty()) {
            Toast.makeText(requireContext(), "Email and password required", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(emailVal, passwordVal)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    try {
                        val userData = ProfileRepository.loadUserProfileSuspend()
                        // ✅ Use the profile if needed
                        findNavController().navigate(R.id.homeFragment)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
