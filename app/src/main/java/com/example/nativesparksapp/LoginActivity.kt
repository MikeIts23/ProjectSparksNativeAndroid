package com.example.nativesparksapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
// (NUOVO) Import per web3j
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageTogglePassword: ImageView
    private lateinit var textForgotPassword: TextView
    private lateinit var textErrorMessage: TextView
    private lateinit var buttonSignIn: Button
    private lateinit var buttonRegister: Button
    private lateinit var imageGoogle: ImageView
    private lateinit var imageApple: ImageView
    private lateinit var progressLoading: ProgressBar

    private var isPasswordVisible = false

    // Firebase Auth
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Google Sign In Client
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val TAG = "LoginActivity"

    companion object {
        const val PREFS_USER_ID = "user_prefs"
        const val KEY_LAST_USER_ID = "last_user_id"

        // (NUOVO) Key per indicare se l'utente ha fatto login via wallet
        const val PREFS_NAME = "UserPrefs"
        const val KEY_REGISTERED_VIA_WALLET = "registered_via_wallet"
    }

    // (NUOVO) Istanza di web3j
    private lateinit var web3j: Web3j

    // (NUOVO) Pulsante o icona per login con wallet
    private lateinit var imageWallet: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        clearAllUserPreferences()

        // Associa le view
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        imageTogglePassword = findViewById(R.id.imageTogglePassword)
        textForgotPassword = findViewById(R.id.textForgotPassword)
        textErrorMessage = findViewById(R.id.textErrorMessage)
        buttonSignIn = findViewById(R.id.buttonSignIn)
        buttonRegister = findViewById(R.id.buttonRegister)
        imageGoogle = findViewById(R.id.imageGoogle)
        imageApple = findViewById(R.id.imageApple)
        progressLoading = findViewById(R.id.progressLoading)

        // (NUOVO) Pulsante/Immagine per login con wallet
        // Assicurati di aver aggiunto l’elemento nel layout login con id imageWallet
        imageWallet = findViewById(R.id.imageWallet)

        // Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // (NUOVO) Inizializza web3j
        web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/YOUR_INFURA_PROJECT_ID"))

        imageTogglePassword.setOnClickListener { togglePasswordVisibility() }

        buttonSignIn.setOnClickListener { signIn() }

        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        textForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password cliccato!", Toast.LENGTH_SHORT).show()
        }

        imageGoogle.setOnClickListener { signInWithGoogle() }

        imageApple.setOnClickListener { signInWithApple() }

        // (NUOVO) Login con wallet
        imageWallet.setOnClickListener { connectWalletLogin() }
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
                        Log.d(TAG, "ID utente salvato dopo login: $userId")
                    }
                    val intent = Intent(this, GameLaunchActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showError(task.exception?.localizedMessage ?: "Errore sconosciuto durante il login.")
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                        Log.d(TAG, "ID utente salvato dopo login con Google: $userId")
                    }
                    val intent = Intent(this, GameLaunchActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showError(task.exception?.localizedMessage ?: "Google sign in failed")
                }
            }
    }

    private fun signInWithApple() {
        Toast.makeText(this, "Login con Apple non implementato.", Toast.LENGTH_SHORT).show()
    }

    // (NUOVO) Login con wallet
    private fun connectWalletLogin() {
        // Qui va la logica di connessione al wallet (MetaMask, WalletConnect, ecc.)
        val address = "0xYourWalletAddressLogin"

        // Salvataggio in SharedPreferences
        val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_REGISTERED_VIA_WALLET, true).apply()

        Toast.makeText(
            this,
            "Login tramite Wallet effettuato. Address: $address",
            Toast.LENGTH_SHORT
        ).show()

        // Vai alla schermata principale
        val intent = Intent(this, GameLaunchActivity::class.java)
        startActivity(intent)
        finish()
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
        Log.d(TAG, "Pulizia delle SharedPreferences al login")

        val profilePrefs = getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        profilePrefs.edit().clear().apply()

        val editProfilePrefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        editProfilePrefs.edit().clear().apply()
    }

    private fun saveCurrentUserId(userId: String) {
        val userPrefs = getSharedPreferences(PREFS_USER_ID, Context.MODE_PRIVATE)
        userPrefs.edit()
            .putString(KEY_LAST_USER_ID, userId)
            .apply()
        Log.d(TAG, "ID utente salvato nelle SharedPreferences: $userId")
    }
}
