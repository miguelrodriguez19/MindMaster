package com.miguelrodriguez19.mindmaster.view.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!
    private lateinit var btnGoToWebsite: MaterialButton
    private lateinit var btnEmailUs: MaterialButton
    private lateinit var btnTerms: MaterialButton
    private lateinit var btnSeeFAQs: MaterialButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        btnGoToWebsite.setOnClickListener {
            openWebsite(getString(R.string.home_url))
        }
        btnEmailUs.setOnClickListener {
            val uriText = getString(R.string.mailto, getString(R.string.company_info_email))
            val uri = Uri.parse(uriText)

            val sendIntent = Intent(Intent.ACTION_SENDTO)
            sendIntent.data = uri
            startActivity(Intent.createChooser(sendIntent, getString(R.string.email_us)))
        }

        btnTerms.setOnClickListener {
            openWebsite(getString(R.string.terms_url))
        }

        btnSeeFAQs.setOnClickListener {
            openWebsite(getString(R.string.help_url))
        }
    }

    private fun openWebsite(uri: String) {
        val webpage: Uri = Uri.parse(uri)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun initWidget() {
        btnGoToWebsite = binding.btnGoToWebsite
        btnEmailUs = binding.btnEmailUs
        btnTerms = binding.btnTermsAndConditions
        btnSeeFAQs = binding.btnSeeFAQs
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
