package com.miguelrodriguez19.mindmaster.views.welcome

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.SyncStateContract.Helpers.update
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentWelcomeBinding
import com.miguelrodriguez19.mindmaster.models.utils.AESEncripter
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.updateHasLoggedInBefore
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvWelcomeTitle: TextView
    private lateinit var tilPassphrase: TextInputLayout
    private lateinit var etPassphrase: EditText
    private lateinit var btnContinue: ExtendedFloatingActionButton
    private lateinit var checkAgreement: MaterialCheckBox
    private lateinit var progressBar: ProgressBar
    private var passphrase: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        (requireActivity() as MainActivity).lockDrawer()
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
        tvWelcomeTitle.text = requireContext().getString(
            R.string.inner_welcome_2_user,
            Preferences.getUser()?.firstName
        )
        setPassphrase()

        tilPassphrase.setEndIconOnClickListener {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.security_phrase), etPassphrase.text)
            clipboard.setPrimaryClip(clip)

            showToast(requireContext(), R.string.copied_2_clipboard)
        }

        btnContinue.setOnClickListener {
            if (checkAgreement.isChecked) {
                checkAgreement.isErrorShown = false
                val fileName = "${getString(R.string.filePassphraseName)}.txt"
                createDocumentResult.launch(fileName)
                saveInFirestoreHash()
                updateHasLoggedInBefore(Preferences.getUser()!!)
            } else {

                checkAgreement.isErrorShown = true
            }
        }

    }

    private fun saveInFirestoreHash() {
        if (passphrase != null) {
            val phraseHash = AESEncripter.generateHash(passphrase!!)
            val iv = AESEncripter.generateInitializationVector()
            Preferences.setInitializationVector(iv)
            Preferences.setSecurePhrase(phraseHash)
            FirebaseManager.saveCredentials(phraseHash, iv)
        }
    }

    private fun setPassphrase() {
        AESEncripter.generateSecurePhrase() {
            passphrase = it
            etPassphrase.setText(it)
        }
    }

    private fun initBinding() {
        tvWelcomeTitle = binding.tvWelcomeTitle
        tilPassphrase = binding.securityPhraseLayout
        etPassphrase = binding.securityPhraseEditText
        btnContinue = binding.btnContinue
        checkAgreement = binding.checkResponsibilityAgreement
        progressBar = binding.progressBarPassphrase
    }

    override fun onStart() {
        super.onStart()
        btnContinue.isEnabled = false
        val countDownTimer = object : CountDownTimer(
            requireContext().getString(R.string.millisecond_2_continue_passphrase).toLong(), 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val s = millisUntilFinished / 1000
                btnContinue.text = requireContext().getString(R.string.continue_timer, s)
            }

            override fun onFinish() {
                btnContinue.isEnabled = true
                btnContinue.text = requireContext().getString(R.string.continue_)
            }
        }

        countDownTimer.start()
    }

    private val createDocumentResult =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                val content = etPassphrase.text.toString()
                context?.contentResolver?.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(content)
                    it.close()
                }
                updateUI()
            }
        }

    private fun updateUI() {
        etPassphrase.text = null
        passphrase = null
        checkAgreement.isChecked = false
        val action =
            WelcomeFragmentDirections.actionWelcomeFragmentToCalendarFragment()
        findNavController().navigate(action)
    }

}