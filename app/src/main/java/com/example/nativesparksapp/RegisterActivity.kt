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

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageTogglePassword: ImageView
    private lateinit var progressLoading: ProgressBar
    private lateinit var buttonSignUp: Button
    private lateinit var textErrorMessage: TextView
    private lateinit var imageGoogle: ImageView
    private lateinit var imageWallet: ImageView

    private var isLoading = false
    private var isPasswordVisible = false

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val TAG = "RegisterActivity"

    companion object {
        const val PREFS_USER_ID = "user_prefs"
        const val KEY_LAST_USER_ID = "last_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        clearAllUserPreferences()

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextName = findViewById(R.id.editTextName)
        editTextPassword = findViewById(R.id.editTextPassword)
        imageTogglePassword = findViewById(R.id.imageTogglePassword)
        progressLoading = findViewById(R.id.progressLoading)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        textErrorMessage = findViewById(R.id.textErrorMessage)
        imageGoogle = findViewById(R.id.imageGoogle)
        imageWallet = findViewById(R.id.imageWallet)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        imageTogglePassword.setOnClickListener { togglePasswordVisibility() }
        buttonSignUp.setOnClickListener { onSignUp() }
        imageGoogle.setOnClickListener { onSignUpWithGoogle() }
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
            imageTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
        }
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    private fun onSignUp() {
        val email = editTextEmail.text.toString().trim()
        val name = editTextName.text.toString().trim()
        val password = editTextPassword.text.toString()

        setLoading(true)
        textErrorMessage.visibility = View.GONE

        if (email.isEmpty() || name.isEmpty() || password.isEmpty()) {
            showErrorMessage("Per favore, compila tutti i campi.")
            setLoading(false)
            return
        }
        if (password.length < 6) {
            showErrorMessage("La password deve contenere almeno 6 caratteri.")
            setLoading(false)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(name)
                        .build()

                    firebaseUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            setLoading(false)
                            Toast.makeText(this, "Registrazione completata!", Toast.LENGTH_SHORT).show()
                            firebaseUser?.uid?.let { userId -> saveCurrentUserId(userId) }
                            startActivity(Intent(this, GameLaunchActivity::class.java))
                            finish()
                        }
                } else {
                    showErrorMessage(
                        task.exception?.localizedMessage
                            ?: "Errore sconosciuto durante la registrazione."
                    )
                    setLoading(false)
                }
            }
    }

    private fun onSignUpWithGoogle() {
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
                showErrorMessage("Google sign in failed: \${e.localizedMessage}")
                setLoading(false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        setLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registrazione con Google completata!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, GameLaunchActivity::class.java))
                    finish()
                } else {
                    showErrorMessage(task.exception?.localizedMessage ?: "Google sign in failed")
                    setLoading(false)
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            progressLoading.visibility = View.VISIBLE
            buttonSignUp.visibility = View.INVISIBLE
        } else {
            progressLoading.visibility = View.GONE
            buttonSignUp.visibility = View.VISIBLE
        }
    }

    private fun showErrorMessage(msg: String) {
        textErrorMessage.text = msg
        textErrorMessage.visibility = View.VISIBLE
    }

    private fun clearAllUserPreferences() {
        val profilePrefs = getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        profilePrefs.edit().clear().apply()
        val editProfilePrefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        editProfilePrefs.edit().clear().apply()
    }

    private fun saveCurrentUserId(userId: String) {
        val userPrefs = getSharedPreferences(PREFS_USER_ID, Context.MODE_PRIVATE)
        userPrefs.edit().putString(KEY_LAST_USER_ID, userId).apply()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "en"))
    }
}
