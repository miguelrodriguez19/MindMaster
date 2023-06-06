package com.miguelrodriguez19.mindmaster.views.settings.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSettingsBinding
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showConfirmationDialog
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager.getAuth
import de.hdodenhof.circleimageview.CircleImageView

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var ivUserPhoto: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var btnEditProfile: ExtendedFloatingActionButton
    private lateinit var swNotifications: SwitchMaterial
    private lateinit var spLanguage: Spinner
    private lateinit var spTheme: Spinner
    private lateinit var spCurrency: Spinner
    private lateinit var btnSecurity: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var btnLogOut: MaterialButton
    private lateinit var btnAdvancedOptions: MaterialButton
    private lateinit var llExpandableAdvancedOptions: LinearLayout
    private lateinit var user: UserResponse

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = Preferences.getUser()!!
        initWidgets()
        setUpData()
        btnEditProfile.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAccountFragment()
            findNavController().navigate(action)
        }
        btnSecurity.setOnClickListener {

        }
        btnChangePassword.setOnClickListener {

        }
        btnLogOut.setOnClickListener {
            showConfirmationDialog(
                requireContext(), getString(R.string.logout_confirmation_title),null
            ) { confirmed ->
                if (confirmed) {
                    logout()
                }
            }

        }

        btnDeleteAccount.setOnClickListener {
            showConfirmationDialog(
                requireContext(),
                getString(R.string.delete_account_confirmation_title),
                getString(R.string.delete_account_confirmation_message)
            ) { confirmed ->
                if (confirmed) {
                    showConfirmationDialog(
                        requireContext(),getString(R.string.confirmation_twice),null
                    ) { confirmed ->
                        if (confirmed) {
                            deleteAccountFromFirestore()
                        }
                    }
                }
            }
        }

        btnAdvancedOptions.setOnClickListener {
            setVisibilityAdvSet()
        }
        llExpandableAdvancedOptions.setOnClickListener {
            setVisibilityAdvSet()
        }
        btnAdvancedOptions.setOnClickListener {
            setVisibilityAdvSet()
        }
        spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // Acciones a realizar cuando se seleccione un elemento del Spinner
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se seleccione ningún elemento del Spinner
            }
        }

        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // Acciones a realizar cuando se seleccione un elemento del Spinner
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se seleccione ningún elemento del Spinner
            }
        }

        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // Acciones a realizar cuando se seleccione un elemento del Spinner
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se seleccione ningún elemento del Spinner
            }
        }
    }

    private fun setUpData() {
        Glide.with(requireActivity())
            .load(user?.photoUrl)
            .into(ivUserPhoto)
        tvName.text = user?.firstName ?: ""

        // TODO()
    }

    private fun deleteAccountFromFirestore() {
        FirebaseManager.deleteUser(user!!)
        logout()
    }

    private fun initWidgets() {
        ivUserPhoto = binding.settingsUserPhoto
        tvName = binding.tvName
        btnEditProfile = binding.efabEditProfile
        swNotifications = binding.swNotifications
        spLanguage = binding.spLanguage
        ArrayAdapter.createFromResource(
            requireContext(), R.array.available_languages, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spLanguage.adapter = adapter
        }
        spTheme = binding.spTheme
        ArrayAdapter.createFromResource(
            requireContext(), R.array.available_themes, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spTheme.adapter = adapter
        }
        spCurrency = binding.spCurrency
        ArrayAdapter.createFromResource(
            requireContext(), R.array.available_currencies, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCurrency.adapter = adapter
        }
        btnSecurity = binding.btnSecurity
        btnChangePassword = binding.btnChangePassword
        btnDeleteAccount = binding.btnDeleteAccount
        llExpandableAdvancedOptions = binding.llExpandableAdvancedOptions
        btnAdvancedOptions = binding.btnAdvancedOptions
        btnLogOut = binding.btnLogOut
    }

    private fun setVisibilityAdvSet() {
        if (llExpandableAdvancedOptions.visibility == View.GONE) {
            llExpandableAdvancedOptions.visibility = View.VISIBLE
            btnAdvancedOptions.icon = requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up_24)
        } else {
            llExpandableAdvancedOptions.visibility = View.GONE
            btnAdvancedOptions.icon = requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down_24)
        }
    }

    private fun logout() {
        getAuth().signOut()
        (requireActivity() as MainActivity).logOut()
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        user = Preferences.getUser()!!
        setUpData()
    }
    override fun onResume() {
        super.onResume()
        user = Preferences.getUser()!!
        setUpData()
    }
}