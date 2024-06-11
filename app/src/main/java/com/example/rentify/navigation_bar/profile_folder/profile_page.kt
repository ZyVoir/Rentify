package com.example.rentify.navigation_bar.profile_folder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Auth

class profile_page : Fragment() {

    private lateinit var profilePic: ImageView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var phoneNumber: EditText
    private lateinit var address: TextView
    private lateinit var payment: TextView
    private lateinit var editButton: Button
    private lateinit var viewProfile : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)
        viewProfile = view

        profilePic = view.findViewById(R.id.image_profile)
        username = view.findViewById(R.id.UserName_profile)
        email = view.findViewById(R.id.UserEmail_profile)

        phoneNumber = view.findViewById(R.id.Phone_Number)
        address = view.findViewById(R.id.Address)
        payment = view.findViewById(R.id.payment_method)
        editButton = view.findViewById(R.id.edit_profile_icon)

        setUserInfo()


        editButton.setOnClickListener {
            val intent = Intent(requireContext(), edit_profile_page::class.java)
            startActivityForResult(intent, 200)
        }

        val logoutButton: Button = view.findViewById(R.id.log_out_button)
        logoutButton.setOnClickListener {
            val auth = Auth(requireContext(), requireActivity())
            auth.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Sign Out", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
            }

        }

        return view
    }

    private fun setUserInfo() {
        val currentUser = MainActivity.currentUser
        if (currentUser.profilePicLink.isNotEmpty()){
            Glide.with(viewProfile).load(currentUser.profilePicLink).into(profilePic)
        }
        username.text = currentUser.username
        email.text = currentUser.email
        phoneNumber.setText(currentUser.phoneNum)
        if (currentUser.addressCity.isNotEmpty()){
            address.text = currentUser.addressStreet + ", " + currentUser.addressCity + ", " + currentUser.addressProvince +
                    ", " + currentUser.addressCountry + ", " + currentUser.addressPostcode
        }
        payment.text = currentUser.paymentMethod
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            setUserInfo()
        }
    }

}