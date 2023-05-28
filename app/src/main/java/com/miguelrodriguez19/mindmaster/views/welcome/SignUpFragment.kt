package com.miguelrodriguez19.mindmaster.views.welcome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSignUpBinding
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showDatePicker
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.createUser
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.PASSWORD_PATTERN

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var etName: EditText
    private lateinit var tilName: TextInputLayout
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etBirthdate: EditText
    private lateinit var tilBirthdate: TextInputLayout
    private lateinit var etPassword: EditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etRepeatPassword: EditText
    private lateinit var tilRepeatPassword: TextInputLayout
    private lateinit var tvError: TextView
    private lateinit var btnSignUp: ExtendedFloatingActionButton
    private lateinit var btnLogIn: Button
    private lateinit var checkTerms: MaterialCheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        (requireActivity() as MainActivity).lockDrawer()
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBindingElements()

        etPassword.addTextChangedListener(pwdWatcher)
        etRepeatPassword.addTextChangedListener(pwdReptWatcher)
        etEmail.addTextChangedListener(emailWatcher)
        etBirthdate.setOnClickListener {
            showDatePicker(requireContext()) { date ->
                etBirthdate.setText(date)
            }
        }
        btnLogIn.setOnClickListener {
            clearFields()
            findNavController().popBackStack()
        }

        btnSignUp.setOnClickListener {
            Toolkit.checkFields(
                requireContext(),
                arrayOf(tilEmail, tilName, tilBirthdate, tilPassword, tilRepeatPassword)
            ) { ok ->
                if (ok) {
                    createUser(
                        requireContext(),
                        etName.text.toString(),
                        etSurname.text.toString() ?: null,
                        etBirthdate.text.toString(),
                        etEmail.text.toString(),
                        etPassword.text.toString()
                    ) { wasAdded ->
                        if (wasAdded) {
                            clearFields()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private val pwdWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrBlank()) {
                if (!s.matches(PASSWORD_PATTERN.toRegex())) {
                    tilPassword.error = getString(R.string.weakPassword)
                } else {
                    tilPassword.error = null
                }
            } else {
                tilPassword.error = null
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val pwdReptWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrBlank()) {
                if (etPassword.text != s) {
                    tilRepeatPassword.error = getString(R.string.pwdsDontMatch)
                    tilPassword.error = getString(R.string.pwdsDontMatch)
                } else {
                    tilPassword.error = null
                    tilRepeatPassword.error = null
                }
            } else {
                tilPassword.error = null
                tilRepeatPassword.error = null
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val emailWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrBlank()) {
                if (!s.matches(PatternsCompat.EMAIL_ADDRESS.toRegex())) {
                    tilEmail.error = getString(R.string.invalidEmail)
                } else {
                    tilEmail.error = null
                }
            } else {
                tilEmail.error = null
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun initBindingElements() {
        etName = binding.txtName
        etSurname = binding.txtSurname
        etEmail = binding.txtEmail
        etBirthdate = binding.txtDateBirthdate
        etPassword = binding.etPassword
        etRepeatPassword = binding.txtRepeatPassword
        tvError = binding.tvError
        btnSignUp = binding.efabSignup
        checkTerms = binding.checkbox
        btnLogIn = binding.btnLogin
        tilName = binding.tilName
        tilEmail = binding.tilEmail
        tilBirthdate = binding.tilDateBirthdate
        tilPassword = binding.tilPassword
        tilRepeatPassword = binding.tilRepeatPassword
    }

    private fun clearFields() {
        etName.text = null
        etSurname.text = null
        etEmail.text = null
        etEmail.error = null
        etBirthdate.text = null
        etPassword.text = null
        etPassword.error = null
        etRepeatPassword.text = null
        etRepeatPassword.error = null
        tilName.error = null
        tilEmail.error = null
        tilBirthdate.error = null
        tilPassword.error = null
        tilRepeatPassword.error = null
        tvError.visibility = View.GONE
        tvError.text = ""
        checkTerms.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}