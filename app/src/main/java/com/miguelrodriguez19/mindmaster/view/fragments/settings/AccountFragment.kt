package com.miguelrodriguez19.mindmaster.view.fragments.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentAccountBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.saveImageInStorage
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.showToast
import de.hdodenhof.circleimageview.CircleImageView

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private var activityRef: MainActivity? = null

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
    private var uriPhoto: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        setUpWithUser()
        activityRef = requireActivity() as MainActivity
        btnSave.setOnClickListener {
            if (areFieldsModified()) {
                updateFirestoreUser(){
                    findNavController().popBackStack()
                }
            } else {
                showToast(requireContext(), R.string.error_unmodified_fields)
            }
        }

        btnEditPhoto.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    getContent.launch("image/*")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    AllDialogs.showAlertDialog(
                        requireContext(), getString(R.string.accept_permissions), getString(
                            R.string.accept_this_permission_to_continue
                        )
                    )
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }
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
                tvName.text = s.toString()
            }
        })
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                uploadImage(uri)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getContent.launch("image/*")
            } else {
                AllDialogs.showAlertDialog(
                    requireContext(), getString(R.string.accept_permissions), getString(
                        R.string.accept_this_permission_to_continue
                    )
                )
            }
        }


    private fun uploadImage(uri: Uri) {
        saveImageInStorage(uri.toString()) { imageUrl ->
            Glide.with(requireActivity())
                .load(uri)
                .into(civUserPhoto)
            uriPhoto = imageUrl
        }
    }

    private fun updateFirestoreUser(onUpdated:(UserResponse)->Unit) {
        progressBar.visibility = View.VISIBLE
        val user = Preferences.getUser()
        user?.let {
            val uri = uriPhoto ?: user.photoUrl
            FirestoreManagerFacade.updateUser(
                user.copy(
                    firstName = etFirstName.text.toString(), lastName = etLastName.text.toString(),
                    birthdate = etBirthdate.text.toString(), photoUrl = uri
                )
            ) { updatedUser ->
                activityRef!!.setUpUser(updatedUser)
                onUpdated(updatedUser)
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun areFieldsModified(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    private fun setUpWithUser() {
        val user = Preferences.getUser()
        user?.let {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etBirthdate.setText(user.birthdate)
            tvName.text = etFirstName.text.toString()

            Glide.with(requireActivity())
                .load(user.photoUrl)
                .into(civUserPhoto)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}