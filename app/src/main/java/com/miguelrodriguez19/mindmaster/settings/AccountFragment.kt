package com.miguelrodriguez19.mindmaster.settings

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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.databinding.FragmentAccountBinding
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

        btnSave.setOnClickListener {
            if (areFieldsModified()) {
                updateFirestoreData()
                findNavController().popBackStack()
            } else {
                // Show error
            }
        }

        btnEditPhoto.setOnClickListener {

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
        // TODO
        progressBar.visibility = View.GONE
    }

    private fun areFieldsModified(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    private fun setUpWithUser() {
        progressBar.visibility = View.VISIBLE
        tvName.text = "ejemplo"
        etFirstName.setText("ejemplo")
        etLastName.setText("ejemplo")
        etBirthdate.setText("ejemplo")



        progressBar.visibility = View.GONE
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