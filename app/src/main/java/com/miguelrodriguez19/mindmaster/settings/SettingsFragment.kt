package com.miguelrodriguez19.mindmaster.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentSettingsBinding
import com.miguelrodriguez19.mindmaster.expenses.ExpensesFragmentDirections
import de.hdodenhof.circleimageview.CircleImageView

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var ivUserPhoto:CircleImageView
    private lateinit var tvName:TextView
    private lateinit var btnEditProfile:ExtendedFloatingActionButton
    private lateinit var swNotifications:SwitchMaterial
    private lateinit var spLanguage:Spinner
    private lateinit var spTheme:Spinner
    private lateinit var spCurrency:Spinner
    private lateinit var btnSecurity:MaterialButton
    private lateinit var btnChangePassword:MaterialButton
    private lateinit var btnLogOut:MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()

        btnEditProfile.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAccountFragment()
            findNavController().navigate(action)
        }
        btnSecurity.setOnClickListener {

        }
        btnChangePassword.setOnClickListener {

        }
        btnLogOut.setOnClickListener {
            showLogoutConfirmationDialog()
        }
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

        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    private fun initWidgets() {
        ivUserPhoto = binding.civUserPhoto
        tvName = binding.tvName
        btnEditProfile = binding.efabEditProfile
        swNotifications = binding.swNotifications
        spLanguage = binding.spLanguage
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.available_languages,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spLanguage.adapter = adapter
        }
        spTheme = binding.spTheme
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.available_themes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spTheme.adapter = adapter
        }
        spCurrency = binding.spCurrency
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.available_currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCurrency.adapter = adapter
        }
        btnSecurity = binding.btnSecurity
        btnChangePassword = binding.btnChangePassword
        btnLogOut = binding.btnLogOut
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.logout_confirmation_message))
        builder.setPositiveButton(getString(R.string.log_out)) { dialog, _ ->
            logout()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun logout() {
        // Borrar la sesión del usuario aquí
        val action = SettingsFragmentDirections.actionSettingsFragmentToLogInFragment()
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}