package com.cometchat.pro.androiduikit

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.cometchat.pro.androiduikit.databinding.ActivityLoginBinding
import com.cometchat.pro.androiduikit.model.UserModel
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.core.Constants
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var pDialog: ProgressDialog? = null
    private lateinit var loggedInUser: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
        initFirebase()
        initFirebaseDatabase()
        initCometChat()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.etPass.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.logInBtn.setOnClickListener(View.OnClickListener { view: View? ->
            login()
        })
    }
    private fun goToSelectActivity() {
        intent = Intent(this@LoginActivity, SelectActivity::class.java);
        startActivity(intent);
    }
    private fun initViews() {
        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }
    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
    }
    private fun initFirebaseDatabase() {
        database = FirebaseDatabase.getInstance(AppConfig.AppDetails.FIREBASE_REALTIME_DATABASE_URL).reference
    }
    private fun initCometChat() {
        val appSettings = AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(
            AppConfig.AppDetails.REGION).build()

        CometChat.init(this, AppConfig.AppDetails.APP_ID, appSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
            }

            override fun onError(e: CometChatException) {
            }
        })
    }
    private fun loginCometChat() {
        if (loggedInUser.uid != null) {
            val uid = loggedInUser.uid
            if (uid != null) {
                CometChat.login(uid, AppConfig.AppDetails.AUTH_KEY, object : CometChat.CallbackListener<User?>() {
                    override fun onSuccess(user: User?) {
                        pDialog!!.dismiss()
                        goToSelectActivity()
                    }
                    override fun onError(e: CometChatException) {
                        pDialog!!.dismiss()
                        Toast.makeText(this@LoginActivity, "Failure to login to CometChat", Toast.LENGTH_LONG)
                    }
                })
            }
        } else {
            pDialog!!.dismiss()
        }
    }
    private fun getUserDetails(email: String?) {
        if (email == null) {
            pDialog!!.dismiss()
            return
        }
        database.child(AppConfig.AppDetails.FIREBASE_USERS).orderByChild(AppConfig.AppDetails.FIREBASE_EMAIL_KEY)
            .equalTo(email)
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        loggedInUser = userSnapshot.getValue(UserModel::class.java)!!
                        if (loggedInUser != null) {
                            loginCometChat()
                        } else {
                            pDialog!!.dismiss()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    pDialog!!.dismiss()
                    Toast.makeText(
                        this@LoginActivity,
                        "Cannot fetch user details information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun callFirebaseAuthService(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                getUserDetails(email)
            } else {
                pDialog!!.dismiss()
                Toast.makeText(
                    this@LoginActivity,
                    "Authentication Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun validateUserCredentials(email: String?, password: String?): Boolean {
        if (email != null && email == "") {
            Toast.makeText(this@LoginActivity, "Please input your email", Toast.LENGTH_LONG).show()
            return false
        }
        if (password != null && password == "") {
            Toast.makeText(this@LoginActivity, "Please input your password", Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }
    private fun login() {
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password = binding.etPass.text.toString().trim { it <= ' ' }
        if (validateUserCredentials(email, password)) {
            pDialog!!.show()
            // call firebase authentication service.
            callFirebaseAuthService(email, password)
        }
    }
    fun createUser(view: View?) {
        startActivity(Intent(this@LoginActivity, CreateUserActivity::class.java))
    }
}