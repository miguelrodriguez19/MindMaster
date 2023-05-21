package com.miguelrodriguez19.mindmaster.views.settings.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentAccountBinding
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast
import de.hdodenhof.circleimageview.CircleImageView

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvName: TextView
    private lateinit var btnEditPhoto: MaterialButton
    private lateinit var civUserPhoto: CircleImageView
    private lateinit var tilFirstName: TextInputLayout
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var tilBirthdate: TextInputLayout
    private lateinit var etBirthdate: EditText
    private lateinit var btnSave: ExtendedFloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var user: UserResponse
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = Preferences.getUser()!!
        initWidgets()
        setUpWithUser()

        btnSave.setOnClickListener {
            if (areFieldsModified()) {
                updateFirestoreData()
                findNavController().popBackStack()
            } else {
                showToast(requireContext(), R.string.error_unmodified_fields)
            }
        }
        btnEditPhoto.setOnClickListener {

        }

        etBirthdate.setOnClickListener {
            AllDialogs.showDatePicker(requireContext()) { date ->
                etBirthdate.setText(date)
            }
        }

        etFirstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                tvName.text = "${s.toString()}"
            }
        })
    }

    private fun updateFirestoreData() {
        progressBar.visibility = View.VISIBLE
        FirebaseManager.updateUser(
            UserResponse(
                user, etFirstName.text.toString(),
                etLastName.text.toString(), etBirthdate.text.toString()
            )
        ) {
            Preferences.setUser(it)
            progressBar.visibility = View.GONE
        }
    }

    private fun areFieldsModified(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    private fun setUpWithUser() {
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)
        etBirthdate.setText(user.birthdate)
        tvName.text = etFirstName.text.toString()

        Glide.with(requireActivity())
            .load(user.photoUrl)
            .into(civUserPhoto)
    }

    private fun initWidgets() {
        tvName = binding.tvName
        btnEditPhoto = binding.btnEditPhoto
        civUserPhoto = binding.civProfileUserPhoto
        etFirstName = binding.txtName
        etLastName = binding.txtSurname
        etBirthdate = binding.txtDateBirthdate
        btnSave = binding.efabSaveChanges
        progressBar = binding.progressBar
    }

    override fun onPause() {
        super.onPause()
        val activity = (requireActivity() as MainActivity)
        activity.userSetUp(Preferences.getUser()!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        val activity = (requireActivity() as MainActivity)
        activity.userSetUp(Preferences.getUser()!!)
    }

    override fun onResume() {
        super.onResume()
        user = Preferences.getUser()!!
        setUpWithUser()
    }
}