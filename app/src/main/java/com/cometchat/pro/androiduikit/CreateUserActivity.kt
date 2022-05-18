package com.cometchat.pro.androiduikit

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.cometchat.pro.androiduikit.databinding.ActivityCreateUserBinding
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
import com.google.firebase.ktx.Firebase

class CreateUserActivity : AppCompatActivity() {
    private var inputUid: TextInputLayout? = null
    private var inputName: TextInputLayout? = null
    private var uid: TextInputEditText? = null
    private var name: TextInputEditText? = null
    private var createUserBtn: MaterialButton? = null
    private var progressBar: ProgressBar? = null
    private var title: TextView? = null
    private var des1: TextView? = null
    private var des2: TextView? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityCreateUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_user)
        inputUid = findViewById(R.id.inputUID)
        inputName = findViewById(R.id.inputName)
        title = findViewById(R.id.tvTitle)
        des1 = findViewById(R.id.tvDes1)
        des2 = findViewById(R.id.tvDes2)
        progressBar = findViewById(R.id.createUser_pb)
        uid = findViewById(R.id.etUID)
        name = findViewById(R.id.etName)
        binding.createUserBtn.setTextColor(resources.getColor(R.color.textColorWhite))
        binding.createUserBtn.setOnClickListener(View.OnClickListener {
            if (binding.etUID.text.toString().isEmpty()) binding.etUID.error = resources.getString(R.string.fill_this_field) else if (binding.etName.text.toString().isEmpty()) binding.etName.error = resources.getString(R.string.fill_this_field) else {
                binding.createUserPb.visibility = View.VISIBLE
                binding.createUserBtn.isClickable = false
                val user = User()
                user.uid = uid!!.text.toString()
                user.name = name!!.text.toString()
                CometChat.createUser(user, AppConfig.AppDetails.AUTH_KEY, object : CallbackListener<User>() {
                    override fun onSuccess(user: User) {
                        login(user)
                    }

                    override fun onError(e: CometChatException) {
                        createUserBtn!!.isClickable = true
                        ErrorMessagesUtils.cometChatErrorMessage(this@CreateUserActivity, e.code)
                    }
                })
            }
        })
    }
    private fun login(user: User) {
        CometChat.login(user.uid, AppConfig.AppDetails.AUTH_KEY, object : CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                startActivity(Intent(this@CreateUserActivity, SelectActivity::class.java))
            }

            override fun onError(e: CometChatException) {
                if (uid != null) Snackbar.make(uid!!.rootView, "Unable to login", Snackbar.LENGTH_INDEFINITE).setAction("Try Again") { startActivity(Intent(this@CreateUserActivity, LoginActivity::class.java)) }.show()
                else ErrorMessagesUtils.cometChatErrorMessage(this@CreateUserActivity, e.code)
            }
        })
    }
}