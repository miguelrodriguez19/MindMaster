package com.miguelrodriguez19.mindmaster.view.fragments.welcome

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.GoogleAuthProvider
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentLogInBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.getAuth
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.logInEmailPwd
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.sendResetPassword
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.checkFields
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.showToast

class LogInFragment : Fragment() {
    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvError: TextView
    private lateinit var btnLogIn: ExtendedFloatingActionButton
    private lateinit var btnGoogle: ExtendedFloatingActionButton
    private lateinit var btnFacebook: ExtendedFloatingActionButton
    private lateinit var btnForgottenPwd: MaterialButton
    private lateinit var btnSignUp: MaterialButton
    private lateinit var progressBar: ProgressBar

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        btnLogIn.setOnClickListener {
            checkFields(requireContext(), arrayOf(binding.tilEmail, binding.tilPassword)) {
                if (it) {
                    progressBar.visibility = View.VISIBLE
                    logInEmailPwd(
                        etEmail.text.toString().trim(), etPassword.text.toString().trim()
                    ) { ok, user ->
                        if (ok) {
                            if (user != null) {
                                updateUI(user)
                            } else {
                                showToast(requireContext(), R.string.try_later)
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            tvError.visibility = View.VISIBLE
                            tvError.text = getString(R.string.wrong_email_password)
                        }
                    }
                }
            }
        }

        btnForgottenPwd.setOnClickListener {
            var emailToReset: String? = null
            val type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            MaterialDialog(requireContext()).show {
                input(inputType = type, hintRes = R.string.hint_email) { dialog, text ->
                    emailToReset = text.toString()
                }
                positiveButton(R.string.confirmation) {
                    emailToReset?.let {
                        if (it.isNotEmpty()) {
                            sendResetPassword(requireContext(), it) { isSuccessful ->
                                if (isSuccessful) {
                                    AllDialogs.showAlertDialog(
                                        context, context.getString(R.string.reset_password),
                                        context.getString(R.string.reset_password_msg)
                                    )
                                } else {
                                    AllDialogs.showAlertDialog(
                                        context, context.getString(R.string.something_went_wrong),
                                        context.getString(R.string.try_later)
                                    )
                                }
                            }
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
    }

    private fun updateUI(user: UserResponse) {
        clearFields()
        val action = if (!user.hasLoggedInBefore) {
            LogInFragmentDirections.actionLogInFragmentToWelcomeFragment(user)
        } else {
            LogInFragmentDirections.actionLogInFragmentToSecurityPhraseLoaderFragment(user)
        }
        findNavController().navigate(action)
    }

    private fun getUserLanguage(): Int {
        return 0
    }

    private fun signInGoogle() {
        progressBar.visibility = View.VISIBLE
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
                        getAuth().signInWithCredential(credential)
                            .addOnCompleteListener { resultTask ->
                                if (resultTask.isSuccessful) {
                                    val gAcc = GoogleSignIn.getLastSignedInAccount(requireContext())
                                    val uid = getAuth().currentUser?.uid

                                    if (uid != null) {
                                        FirestoreManagerFacade.getUserByUID(uid) {
                                            val actualUser = it
                                                ?: UserResponse(
                                                    uid, gAcc?.givenName!!, gAcc.familyName,
                                                    gAcc.email!!, null, gAcc.photoUrl!!.toString(),
                                                    false
                                                )
                                            updateUI(actualUser)
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        resultTask.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressBar.visibility = View.GONE
                                }
                            }
                    }
                } else {
                    progressBar.visibility = View.GONE
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
        progressBar = binding.progressBarLogIn
    }

    private fun clearFields() {
        binding.tilEmail.isErrorEnabled = false
        etEmail.text = null
        etEmail.error = null
        binding.tilPassword.isErrorEnabled = false
        etPassword.text = null
        etPassword.error = null
        tvError.text = ""
        progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}