package com.miguelrodriguez19.mindmaster.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentLogInBinding

class LogInFragment : Fragment() {
    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    private lateinit var etEmail:EditText
    private lateinit var etPassword:EditText
    private lateinit var btnLogIn: ExtendedFloatingActionButton
    private lateinit var btnGoogle: ExtendedFloatingActionButton
    private lateinit var btnFacebook: ExtendedFloatingActionButton
    private lateinit var btnForgottenPwd: Button
    private lateinit var btnSignUp: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        initBindingElements()

        btnSignUp.setOnClickListener {
            val action = LogInFragmentDirections.actionLogInFragmentToSignUpFragment()
            findNavController().navigate(action)
        }


    }

    private fun initBindingElements() {
        etEmail = binding.txtEmailLogIn
        etPassword = binding.txtPasswordLogIn
        btnLogIn = binding.efabLogin
        btnGoogle = binding.efabGoogle
        btnFacebook = binding.efabFacebook
        btnForgottenPwd = binding.btnForgottenPassword
        btnSignUp = binding.btnSignUp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}