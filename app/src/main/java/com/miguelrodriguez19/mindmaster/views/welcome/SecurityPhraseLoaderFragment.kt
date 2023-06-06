package com.miguelrodriguez19.mindmaster.views.welcome

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSecurityPhraseLoaderBinding
import com.miguelrodriguez19.mindmaster.models.utils.AESEncripter
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class SecurityPhraseLoaderFragment : Fragment() {
    private var _binding: FragmentSecurityPhraseLoaderBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvWelcomeTitle: TextView
    private lateinit var tvFileName: TextView
    private lateinit var tvError: TextView
    private lateinit var tilPassphrase: TextInputLayout
    private lateinit var etPassphrase: EditText
    private lateinit var btnContinue: ExtendedFloatingActionButton
    private lateinit var btnOptions: MaterialButton
    private lateinit var btnAttach: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var cvAttachZone: MaterialCardView
    private lateinit var progressBar: ProgressBar
    private lateinit var menu: PopupMenu
    private var expectedHash: String? = null
    private var fileContent: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        (requireActivity() as MainActivity).lockDrawer()
        _binding = FragmentSecurityPhraseLoaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
        tvWelcomeTitle.text = requireContext().getString(
            R.string.inner_welcome_back_2_user,
            Preferences.getUser()?.firstName
        )

        btnContinue.setOnClickListener {
            if (etPassphrase.text.toString().isNotEmpty() || !fileContent.isNullOrEmpty()) {
                progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    expectedHash = FirebaseManager.getSecurePhraseHash()
                }.invokeOnCompletion {
                    val phrase = fileContent ?: etPassphrase.text.toString()
                    val inputHash =
                        AESEncripter.generateHash(phrase)
                    if (inputHash == expectedHash) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val iVector =
                                withContext(Dispatchers.Default) {
                                    FirebaseManager.getInitialisationVector()
                                }
                            if (iVector != null) {
                                val main = (requireActivity() as MainActivity)
                                main.updateSecurityPreferences(phrase, iVector)
                                withContext(Dispatchers.Main){
                                    updateUI()
                                }
                            } else {
                                progressBar.visibility = View.GONE
                                showError(R.string.try_later)
                            }
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        showError(R.string.incorrect_secure_phrase_err)
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                showError(R.string.fill_secure_phrase_or_attach_file)
            }
        }

        cvAttachZone.setOnClickListener {
            getContent.launch("text/*")
        }

        btnAttach.setOnClickListener {
            getContent.launch("text/*")
        }

        btnClear.setOnClickListener {
            clearFile()
        }

        btnOptions.setOnClickListener {
            menu = PopupMenu(requireContext(), view)
            menu.menuInflater.inflate(R.menu.security_phrase_loader_context_menu, menu.menu)
            menu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.change_account -> {
                        (requireActivity() as MainActivity).logOut()
                        true
                    }
                    R.id.help -> {
                        //TODO("Enviar a la pagina web con las FAQs")
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
            menu.show()
        }
    }

    private fun clearFile() {
        tvFileName.text = null
        fileContent = null
    }

    private fun updateUI() {
        etPassphrase.text = null
        expectedHash = null
        tvFileName.text = null
        fileContent = null
        hideError()
        val action =
            SecurityPhraseLoaderFragmentDirections.actionSecurityPhraseLoaderFragmentToCalendarFragment()
        findNavController().navigate(action)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            hideError()
            clearFile()
            if (uri != null) {
                if (requireContext().contentResolver.getType(uri) == "text/plain") {
                    try {
                        val docFile = DocumentFile.fromSingleUri(requireContext(), uri)
                        tvFileName.text = docFile?.name ?: getString(R.string.unknown_file_name)
                        requireContext().contentResolver.openInputStream(uri)
                            ?.use { inputStream ->
                                val reader = BufferedReader(InputStreamReader(inputStream))
                                fileContent = reader.use { it.readText() }
                            }
                    } catch (e: IOException) {
                        clearFile()
                        showError(R.string.reading_file_err)
                    }
                } else {
                    showError(R.string.invalid_file_type_err)
                }
            }
        }

    private fun showError(message: Int) {
        tvError.visibility = View.VISIBLE
        tvError.text = getString(message)
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }

    private fun initBinding() {
        tvWelcomeTitle = binding.tvWelcomeTitle
        tilPassphrase = binding.securityPhraseLayout
        etPassphrase = binding.securityPhraseEditText
        btnContinue = binding.btnContinue
        progressBar = binding.progressBarPassphrase
        tvFileName = binding.tvFileName
        tvError = binding.tvError
        btnAttach = binding.btnAttachFile
        cvAttachZone = binding.cvAttachFile
        btnOptions = binding.btnMoreOptions
        btnClear = binding.btnClearFile
    }
}