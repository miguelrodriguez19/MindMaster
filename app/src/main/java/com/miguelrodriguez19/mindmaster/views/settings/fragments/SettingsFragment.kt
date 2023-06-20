package com.miguelrodriguez19.mindmaster.views.settings.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSettingsBinding
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.getAuth
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showConfirmationDialog
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.utils.Preferences.getUser
import com.miguelrodriguez19.mindmaster.models.utils.Preferences.setCurrency
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast
import de.hdodenhof.circleimageview.CircleImageView

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var ivUserPhoto: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var btnEditProfile: ExtendedFloatingActionButton
    private lateinit var swNotifications: SwitchMaterial
    private lateinit var spTheme: Spinner
    private lateinit var spCurrency: Spinner
    private lateinit var btnSecurity: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var btnLogOut: MaterialButton
    private lateinit var btnAdvancedOptions: MaterialButton
    private lateinit var llExpandableAdvancedOptions: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        setUpData(getUser())
        initSpinners()

        btnEditProfile.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAccountFragment()
            findNavController().navigate(action)
        }

        btnSecurity.setOnClickListener {
            showToast(requireContext(), R.string.under_development)
            TODO("Show a bottom sheet to choose security options for the app")
        }

        btnChangePassword.setOnClickListener {
            val user = getUser()
            user?.let {
                FirebaseManager.sendResetPassword(requireContext(), user.email) { isSuccessful ->
                    if (isSuccessful) {
                        AllDialogs.showAlertDialog(
                            requireContext(), requireContext().getString(R.string.reset_password),
                            requireContext().getString(R.string.reset_password_msg)
                        )
                    } else {
                        AllDialogs.showAlertDialog(
                            requireContext(),
                            requireContext().getString(R.string.something_went_wrong),
                            requireContext().getString(R.string.try_later)
                        )
                    }
                }
            }
        }

        btnLogOut.setOnClickListener {
            showConfirmationDialog(
                requireContext(), getString(R.string.logout_confirmation_title), null
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
                        requireContext(), getString(R.string.confirmation_twice), null
                    ) { ok ->
                        if (ok) {
                            deleteAccountFromFirestore()
                        }
                    }
                }
            }
        }

        btnAdvancedOptions.setOnClickListener {
            setVisibilityAdvSet()
        }


        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                Preferences.saveTheme(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No es necesario implementar esto si no necesitas realizar ninguna acción cuando no se selecciona ningún elemento
            }
        }


        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    0 -> setCurrency("€")
                    1 -> setCurrency("$")
                    2 -> setCurrency("£")
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se seleccione ningún elemento del Spinner
            }
        }
    }

    private fun setUpData(user: UserResponse?) {
        if (user != null) {
            Glide.with(requireActivity())
                .load(user.photoUrl)
                .into(ivUserPhoto)

            tvName.text = user.firstName
        }
    }

    private fun deleteAccountFromFirestore() {
        val user = getUser()
        user?.let {
            FirebaseManager.deleteUser(user)
        }
        logout()
    }

    private fun initWidgets() {
        ivUserPhoto = binding.settingsUserPhoto
        tvName = binding.tvName
        btnEditProfile = binding.efabEditProfile
        swNotifications = binding.swNotifications
        spTheme = binding.spTheme
        spCurrency = binding.spCurrency
        btnSecurity = binding.btnSecurity
        btnChangePassword = binding.btnChangePassword
        btnDeleteAccount = binding.btnDeleteAccount
        llExpandableAdvancedOptions = binding.llExpandableAdvancedOptions
        btnAdvancedOptions = binding.btnAdvancedOptions
        btnLogOut = binding.btnLogOut

        val user = getAuth().currentUser
        user?.let {
            val providerData = it.providerData
            for (userInfo in providerData) {
                if (userInfo.providerId == "password") {
                    // User is signed in with Email/Password
                    // So it has sense to let him reset password
                    btnChangePassword.visibility = View.VISIBLE
                    break
                } else {
                    // User is signed in with other provider
                    btnChangePassword.visibility = View.GONE
                }
            }
        }
    }

    private fun initSpinners() {
        ArrayAdapter.createFromResource(
            requireContext(), R.array.available_themes, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spTheme.adapter = adapter
        }
        spTheme.setSelection(Preferences.getTheme().toInt())

        val availableCurrencies = resources.getStringArray(R.array.available_currencies)
        val currentCurrency = Preferences.getCurrency()
        val currencyIndex = availableCurrencies.indexOf(currentCurrency)

        ArrayAdapter.createFromResource(
            requireContext(), R.array.available_currencies, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCurrency.adapter = adapter
        }
        spCurrency.setSelection(currencyIndex)
    }

    private fun setVisibilityAdvSet() {
        if (llExpandableAdvancedOptions.visibility == View.GONE) {
            llExpandableAdvancedOptions.visibility = View.VISIBLE
            btnAdvancedOptions.icon =
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_keyboard_arrow_up_24)
        } else {
            llExpandableAdvancedOptions.visibility = View.GONE
            btnAdvancedOptions.icon = AppCompatResources.getDrawable(
                requireContext(), R.drawable.ic_keyboard_arrow_down_24
            )
        }
    }

    private fun logout() {
        (requireActivity() as MainActivity).logOut()
    }

    override fun onResume() {
        super.onResume()
        setUpData(getUser())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}