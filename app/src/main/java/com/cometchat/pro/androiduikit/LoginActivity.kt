package com.cometchat.pro.androiduikit

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.cometchat.pro.androiduikit.model.usermodel
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Constants
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    companion object{
        private val TAG = LoginActivity::class.java.simpleName
    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initFirebase()
        initFirebaseDatabase()
        initCometChat()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.etUID.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputUID.endIconDrawable = getDrawable(R.drawable.ic_arrow_right_24dp)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etUID.setOnEditorActionListener(OnEditorActionListener { textView: TextView?, i: Int, keyEvent: KeyEvent? ->
            var uid = binding.etUID.text.toString().trim()
            var password = binding.etPass.text.toString().trim()
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (uid.isEmpty()) {
                    binding.inputUID.endIconDrawable = null
                    binding.etUID.error = resources.getString(R.string.fill_this_field)
                } else {
                    binding.loginProgress.visibility = View.VISIBLE
                    binding.inputUID.isEndIconVisible = false
                    loginComet(uid)
                }
            }
            true
        })

        binding.logInBtn.setOnClickListener(View.OnClickListener { view: View? ->
            var uid = binding.etUID.text.toString().trim()
            var password = binding.etPass.text.toString().trim()
            if (uid.isEmpty()) {
                binding.inputUID.endIconDrawable = null
                binding.etUID.error = resources.getString(R.string.fill_this_field)
            } else {
                findViewById<View>(R.id.loginProgress).visibility = View.VISIBLE
                binding.inputUID.isEndIconVisible = false
                loginComet(uid)
            }
            usermodel().uid = uid
            usermodel().name = password
            database = Firebase.database.reference
            database.child("Users").child(uid).setValue(usermodel())

        })
    }
    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
    }
    private fun initFirebaseDatabase() {
        database = FirebaseDatabase.getInstance(AppConfig.AppDetails.FIREBASE_REALTIME_DATABASE_URL).getReference()
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
    private fun loginComet(uid: String) {
        CometChat.login(uid, AppConfig.AppDetails.AUTH_KEY, object : CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                startActivity(Intent(this@LoginActivity, SelectActivity::class.java))
                finish()
            }
            override fun onError(e: CometChatException) {
                binding.inputUID.isEndIconVisible = true
                Log.e("login", "onError login: "+e.code)
                findViewById<View>(R.id.loginProgress).visibility = View.GONE
                ErrorMessagesUtils.cometChatErrorMessage(this@LoginActivity, e.code)
            }
        })
    }
    fun createUser(view: View?) {
        startActivity(Intent(this@LoginActivity, CreateUserActivity::class.java))
    }
}