package com.miguelrodriguez19.mindmaster.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
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
    private lateinit var spLanguage: Spinner

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

        btnLogIn.setOnClickListener {
            if (checkLogIn()){
                val action = LogInFragmentDirections.actionLogInFragmentToCalendarFragment()
                findNavController().navigate(action)
            }
        }

        btnSignUp.setOnClickListener {
            val action = LogInFragmentDirections.actionLogInFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.available_languages,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spLanguage.adapter = adapter
        }

        spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Acciones a realizar cuando se seleccione un elemento del Spinner
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se seleccione ningún elemento del Spinner
            }
        }


    }

    private fun checkLogIn(): Boolean {
        // Comprobar campos ...

        return true
    }

    private fun initBindingElements() {
        etEmail = binding.txtEmailLogIn
        etPassword = binding.txtPasswordLogIn
        btnLogIn = binding.efabLogin
        btnGoogle = binding.efabGoogle
        btnFacebook = binding.efabFacebook
        btnForgottenPwd = binding.btnForgottenPassword
        btnSignUp = binding.btnSignUp
        spLanguage = binding.spLanguage
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}