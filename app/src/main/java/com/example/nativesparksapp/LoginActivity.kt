package com.example.nativesparksapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageTogglePassword: ImageView
    private lateinit var textForgotPassword: TextView
    private lateinit var textErrorMessage: TextView
    private lateinit var buttonSignIn: Button
    private lateinit var buttonRegister: Button
    private lateinit var imageGoogle: ImageView
    private lateinit var progressLoading: ProgressBar
    private lateinit var imageWallet: ImageView

    private var isPasswordVisible = false

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    companion object {
        const val PREFS_USER_ID = "user_prefs"
        const val KEY_LAST_USER_ID = "last_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        clearAllUserPreferences()

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        imageTogglePassword = findViewById(R.id.imageTogglePassword)
        textForgotPassword = findViewById(R.id.textForgotPassword)
        textErrorMessage = findViewById(R.id.textErrorMessage)
        buttonSignIn = findViewById(R.id.buttonSignIn)
        buttonRegister = findViewById(R.id.buttonRegister)
        imageGoogle = findViewById(R.id.imageGoogle)
        progressLoading = findViewById(R.id.progressLoading)
        imageWallet = findViewById(R.id.imageWallet)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        imageTogglePassword.setOnClickListener { togglePasswordVisibility() }
        buttonSignIn.setOnClickListener { signIn() }
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        textForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password cliccato!", Toast.LENGTH_SHORT).show()
        }
        imageGoogle.setOnClickListener { signInWithGoogle() }
        imageWallet.setOnClickListener { }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            editTextPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            editTextPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageTogglePassword.setImageResource(android.R.drawable.ic_secure)
        }
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    private fun signIn() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Per favore, compila tutti i campi.")
            return
        }

        textErrorMessage.visibility = View.GONE
        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { userId ->
                        saveCurrentUserId(userId)
                    }
                    startActivity(Intent(this, GameLaunchActivity::class.java))
                    finish()
                } else {
                    showError(task.exception?.localizedMessage ?: "Errore sconosciuto durante il login.")
                }
            }
    }

    private fun signInWithGoogle() {
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showError("Google sign in failed: ${e.localizedMessage}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { userId ->
                        saveCurrentUserId(userId)
                    }
                    startActivity(Intent(this, GameLaunchActivity::class.java))
                    finish()
                } else {
                    showError(task.exception?.localizedMessage ?: "Google sign in failed")
                }
            }
    }

    private fun showError(message: String) {
        textErrorMessage.visibility = View.VISIBLE
        textErrorMessage.text = message
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            progressLoading.visibility = View.VISIBLE
            buttonSignIn.visibility = View.INVISIBLE
        } else {
            progressLoading.visibility = View.GONE
            buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun clearAllUserPreferences() {
        getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
        getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    private fun saveCurrentUserId(userId: String) {
        getSharedPreferences(PREFS_USER_ID, Context.MODE_PRIVATE)
            .edit().putString(KEY_LAST_USER_ID, userId).apply()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "en"))
    }
}
