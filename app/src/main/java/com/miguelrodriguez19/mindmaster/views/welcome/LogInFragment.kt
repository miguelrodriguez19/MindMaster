package com.miguelrodriguez19.mindmaster.views.welcome

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.GoogleAuthProvider
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentLogInBinding
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.getAuth
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.logInEmailPwd
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.checkFields


class LogInFragment : Fragment() {
    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvError: TextView
    private lateinit var btnLogIn: ExtendedFloatingActionButton
    private lateinit var btnGoogle: ExtendedFloatingActionButton
    private lateinit var btnFacebook: ExtendedFloatingActionButton
    private lateinit var btnForgottenPwd: Button
    private lateinit var btnSignUp: Button
    private lateinit var spLanguage: Spinner

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).lockDrawer()
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        initBindingElements()
        view.findViewById<LinearLayout>(R.id.ll_logo).setOnClickListener {
            etEmail.setText("mr916086@gmail.com")
            etPassword.setText("Abcd1234")
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        btnLogIn.setOnClickListener {
            checkFields(requireContext(), arrayOf(binding.tilEmail, binding.tilPassword)) {
                if (it) {
                    logInEmailPwd(requireActivity(),
                        etEmail.text.toString(),
                        etPassword.text.toString()
                    ) { ok ->
                        if (ok) {
                            updateUI()
                        } else {
                            tvError.visibility = View.VISIBLE
                            tvError.text = getString(R.string.wrong_email_password)
                        }
                    }
                }
            }
        }

        btnSignUp.setOnClickListener {
            val action = LogInFragmentDirections.actionLogInFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        btnGoogle.setOnClickListener {
            signInGoogle()
        }

        ArrayAdapter.createFromResource(
            requireContext(), R.array.available_languages, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spLanguage.adapter = adapter
        }
        spLanguage.setSelection(getUserLanguage())
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

    private fun updateUI() {
        clearFields()
        val action = LogInFragmentDirections.actionLogInFragmentToCalendarFragment()
        findNavController().navigate(action)
    }

    private fun getUserLanguage(): Int {
        return 0;
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        getAuth().signInWithCredential(credential).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                                val uid = getAuth().currentUser?.uid!!
                                val actualUser = UserResponse(
                                    uid,account?.givenName!!,account.familyName,
                                    account.email!!,null,account.photoUrl!!.toString()
                                )
                                FirebaseManager.saveUser(actualUser)
                                Preferences.setUser(actualUser)
                                (requireActivity() as MainActivity).userSetUp(actualUser)
                                val action =
                                    LogInFragmentDirections.actionLogInFragmentToCalendarFragment()
                                findNavController().navigate(action)
                                clearFields()
                            } else {
                                Toast.makeText(
                                    context,
                                    task.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun initBindingElements() {
        etEmail = binding.txtEmailLogIn
        etPassword = binding.txtPasswordLogIn
        tvError = binding.tvError
        btnLogIn = binding.efabLogin
        btnGoogle = binding.efabGoogle
        btnFacebook = binding.efabFacebook
        btnForgottenPwd = binding.btnForgottenPassword
        btnSignUp = binding.btnSignUp
        spLanguage = binding.spLanguage
    }

    private fun clearFields() {
        etEmail.text = null
        etEmail.error = null
        etPassword.text = null
        etPassword.error = null
        tvError.text = ""
        tvError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}