package com.miguelrodriguez19.mindmaster.welcome

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSignUpBinding
import com.miguelrodriguez19.mindmaster.utils.AllDialogs
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.PASSWORD_PATTERN

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
            showDatePickerDialog()
        }

        btnLogIn.setOnClickListener {
            clearFields()
            findNavController().popBackStack()
        }

        btnSignUp.setOnClickListener {
            checkFields() { ok ->
                if (ok) {
                    createUserFirebase() { wasAdded ->
                        if (wasAdded) {
                            clearFields()
                            findNavController().popBackStack()
                        }
                    }
                } else {
                    //tvError.visibility = View.VISIBLE
                    //tvError.text = getString(R.string.fill_all_fields)
                }
            }
        }
    }


    private fun createUserFirebase(result: (Boolean) -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(),
            etPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                result(true)
            } else {
                result(false)
                val msg = if (it.exception is FirebaseAuthUserCollisionException) {
                    getString(R.string.collision_auth)
                } else {
                    getString(R.string.something_went_wrong)
                }
                AllDialogs.showAlertDialog(
                    requireContext(),
                    getString(R.string.error),
                    msg
                )
            }
        }
    }


    private fun checkFields(callback: (Boolean) -> Unit) {
        var flag = true
        for (item in arrayOf(tilEmail, tilName, tilBirthdate, tilPassword, tilRepeatPassword)) {
            if (item.editText!!.text.isBlank()) {
                item.error = getString(R.string.fill_this_field)
                flag = false
            } else {
                item.error = null
            }
        }
        checkTerms.isErrorShown = !checkTerms.isChecked

        callback(flag && checkTerms.isChecked)
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(requireActivity().supportFragmentManager, "datePicker")
    }

    private fun onDateSelected(day: Int, month: Int, year: Int) {
        val myMonth = if ((month + 1) < 10) {
            "0${month + 1}"
        } else {
            month + 1
        }

        val myDay = if (day < 10) {
            "0$day"
        } else {
            day
        }
        val date = "$year-${myMonth}-$myDay"
        etBirthdate.setText(date)
    }

    private val pwdWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isNotBlank() && !PASSWORD_PATTERN.matcher(s.toString()).matches()) {
                tilPassword.error = getString(R.string.weakPassword)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val pwdReptWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isNotBlank() || etPassword.text != s) {
                tilRepeatPassword.error = getString(R.string.pwdsDontMatch)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val emailWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isNotBlank() && !PatternsCompat.EMAIL_ADDRESS.matcher(s.toString())
                    .matches()
            ) {
                tilEmail.error = getString(R.string.invalidEmail)
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