package com.cometchat.pro.androiduikit

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.cometchat.pro.androiduikit.databinding.ActivityCreateUserBinding
import com.cometchat.pro.androiduikit.model.UserModel
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var pDialog: ProgressDialog? = null
    private lateinit var binding: ActivityCreateUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        auth = Firebase.auth
        initViews()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_user)
        binding.etPass.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.etConfPass.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.createUserBtn.setTextColor(resources.getColor(R.color.textColorWhite))
        binding.createUserBtn.setOnClickListener(View.OnClickListener {
            register()
        })
    }
    private fun initViews() {
        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }
    private fun validate(fullName: String?, email: String?, password: String?, confirmPassword: String?): Boolean {
        if (fullName == null || fullName.equals("")) {
            Toast.makeText(this@CreateUserActivity, "Please input your full name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (email == null || email.equals("")) {
            Toast.makeText(this@CreateUserActivity, "Please input your email", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password == null || password.equals("")) {
            Toast.makeText(this@CreateUserActivity, "Please input your password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (confirmPassword == null || confirmPassword.equals("")) {
            Toast.makeText(this@CreateUserActivity, "Please input your confirm password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this@CreateUserActivity, "Your password and confirm password must be matched", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    private fun goToLoginActivity() {
        intent = Intent(this@CreateUserActivity, LoginActivity::class.java)
        startActivity(intent)
    }
    private fun createCometChatAccount(userId: String?, fullname: String?) {
        val authKey = AppConfig.AppDetails.AUTH_KEY // Replace with your API Key.
        val user = User()
        user.uid = userId // Replace with your uid for the user to be created.
        user.name = fullname // Replace with the name of the user
        CometChat.createUser(user, authKey, object : CallbackListener<User>() {
            override fun onSuccess(user: User) {
                pDialog!!.dismiss()
                Toast.makeText(this@CreateUserActivity, fullname + " was created successfully", Toast.LENGTH_LONG).show()
                goToLoginActivity()
            }
            override fun onError(e: CometChatException) {
                pDialog!!.dismiss()
                Log.d("Error", e.printStackTrace().toString())
                Toast.makeText(this@CreateUserActivity, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun insertFirebaseDatabase(userId: String?, fullname: String?, email: String?) {
        val userModel = UserModel()
        userModel.uid = userId
        userModel.name = fullname
        userModel.email = email
        database = Firebase.database.reference;
        database.child("users").child(userId!!).setValue(userModel)
    }
    private fun createFirebaseAccount(fullname: String?, email: String?, password: String?) {
        if (email != null && password != null) {
            auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = UUID.randomUUID()
                        insertFirebaseDatabase(userId.toString(), fullname, email)
                        createCometChatAccount(userId.toString(), fullname)
                    } else {
                        pDialog!!.dismiss()
                        Toast.makeText(this@CreateUserActivity, "Cannot create your account, please try again", Toast.LENGTH_LONG).show();
                    }
                }
        } else {
            pDialog!!.dismiss()
            Toast.makeText(this@CreateUserActivity, "Please provide your email and password", Toast.LENGTH_LONG).show();
        }
    }
    private fun register() {
        val fullName = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPass.text.toString().trim()
        val confirmPassword = binding.etConfPass.text.toString().trim()
        if (validate(fullName, email, password, confirmPassword)) {
            pDialog!!.show()
            createFirebaseAccount(fullName,email, password)
        }
    }
}