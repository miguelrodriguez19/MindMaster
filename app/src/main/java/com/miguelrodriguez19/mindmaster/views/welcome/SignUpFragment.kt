package com.miguelrodriguez19.mindmaster.views.welcome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showAlertDialog
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showDatePicker
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.createUser
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.PASSWORD_PATTERN
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

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
    private lateinit var progressBar: ProgressBar

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
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val birthdate = LocalDate.parse(date, formatter)
                val now = LocalDate.now()
                val period = Period.between(birthdate, now)
                val minAge = getString(R.string.minAgeToRegister).toInt()
                if (period.years < minAge) {
                    tilBirthdate.isErrorEnabled = true
                    tilBirthdate.error = getString(R.string.minAgeError, minAge)
                } else {
                    tilBirthdate.error = null
                    tilBirthdate.isErrorEnabled = false
                    etBirthdate.setText(date)
                }
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
                    if (checkTerms.isChecked) {
                        progressBar.visibility = View.VISIBLE
                        createUser(
                            requireContext(), etName.text.toString(),
                            etSurname.text.toString(), etBirthdate.text.toString(),
                            etEmail.text.toString(), etPassword.text.toString()
                        ) { wasAdded ->
                            if (wasAdded) {
                                clearFields()
                                showAlertDialog(
                                    requireContext(), getString(R.string.verify_email),
                                    getString(R.string.verify_email_message)
                                )
                                progressBar.visibility = View.GONE
                                findNavController().popBackStack()
                            }
                        }
                    } else {
                        checkTerms.isErrorShown = true
                        checkTerms.error = requireContext().getString(R.string.field_obligatory)
                        showToast(requireContext(), R.string.check_terms_and_conditions)
                    }
                }
            }
        }
    }

    private val pwdWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrBlank()) {
                if (!s.toString().matches(PASSWORD_PATTERN.toRegex())) {
                    tilPassword.isErrorEnabled = true
                    tilPassword.error = getString(R.string.weakPassword)
                } else {
                    tilPassword.isErrorEnabled = false
                    tilPassword.error = null
                    if (etRepeatPassword.text.toString() != s.toString()) {
                        tilRepeatPassword.isErrorEnabled = true
                        tilRepeatPassword.error = getString(R.string.pwdsDontMatch)
                    } else {
                        tilRepeatPassword.error = null
                        resetErrorEnabled()
                    }
                }
            } else {
                tilPassword.isErrorEnabled = false
                tilPassword.error = null
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val pwdReptWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.isNullOrBlank() || etPassword.text.toString() == s.toString()) {
                tilRepeatPassword.error = null
                tilPassword.error = null
                resetErrorEnabled()
            } else {
                tilRepeatPassword.isErrorEnabled = true
                tilRepeatPassword.error = getString(R.string.pwdsDontMatch)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun resetErrorEnabled() {
        tilPassword.isErrorEnabled = false
        tilRepeatPassword.isErrorEnabled = false
    }

    private val emailWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrBlank()) {
                if (!s.toString().matches(PatternsCompat.EMAIL_ADDRESS.toRegex())) {
                    tilEmail.isErrorEnabled = true
                    tilEmail.error = getString(R.string.invalidEmail)
                } else {
                    tilEmail.isErrorEnabled = false
                    tilEmail.error = null
                }
            } else {
                tilEmail.isErrorEnabled = false
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
        progressBar = binding.progressBarSignUp
    }

    private fun clearFields() {
        val editTexts =
            listOf(etName, etSurname, etEmail, etBirthdate, etPassword, etRepeatPassword)
        val textInputLayouts =
            listOf(tilName, tilEmail, tilBirthdate, tilPassword, tilRepeatPassword)

        for (editText in editTexts) {
            editText.text = null
            editText.error = null
        }

        for (textInputLayout in textInputLayouts) {
            textInputLayout.error = null
            textInputLayout.isErrorEnabled = false
        }

        tvError.apply {
            visibility = View.GONE
            text = ""
        }

        checkTerms.isChecked = false
        checkTerms.isErrorShown = false
        checkTerms.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}