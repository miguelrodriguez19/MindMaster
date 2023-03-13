package com.miguelrodriguez19.mindmaster.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthdate: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRepeatPassword: EditText
    private lateinit var btnSignUp: ExtendedFloatingActionButton
    private lateinit var btnGoogle: ExtendedFloatingActionButton
    private lateinit var btnFacebook: ExtendedFloatingActionButton
    private lateinit var btnLogIn: Button
    private lateinit var checkTerms: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        initBindingElements()

        btnLogIn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initBindingElements() {
        etName = binding.txtName
        etSurname = binding.txtSurname
        etEmail = binding.txtEmail
        etPhone = binding.txtPhone
        etBirthdate = binding.txtDateBirthdate
        etPassword = binding.txtPassword
        etRepeatPassword = binding.txtRepeatPassword
        btnSignUp = binding.efabSignup
        btnGoogle = binding.efabGoogle
        btnFacebook = binding.efabFacebook
        checkTerms = binding.checkbox
        btnLogIn = binding.btnLogin
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}