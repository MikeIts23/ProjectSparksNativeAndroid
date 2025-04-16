package com.example.nativesparksapp

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager

class LoginActivity : AppCompatActivity()  {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageTogglePassword: ImageView
    private lateinit var textForgotPassword: TextView
    private lateinit var textErrorMessage: TextView
    private lateinit var buttonSignIn: Button
    private lateinit var buttonRegister: Button
    private lateinit var imageGoogle: ImageView
    private lateinit var progressLoading: ProgressBar

    private var isPasswordVisible = false

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val TAG = "LoginActivity"

    companion object {
        const val PREFS_USER_ID = "user_prefs"
        const val KEY_LAST_USER_ID = "last_user_id"
        const val PREFS_NAME = "UserPrefs"
        const val KEY_REGISTERED_VIA_WALLET = "registered_via_wallet"
        const val KEY_WALLET_ADDRESS = "wallet_address"
    }

    // Rimosso web3j in quanto non necessario per Solana.
    private lateinit var imageWallet: ImageView

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

        // Qui sostituiamo l’integrazione con MetaMask con Phantom (o altro wallet Solana)
        imageWallet.setOnClickListener { connectWithSolanaWallet() }

        // Gestisci l'intent se l'Activity è stata avviata da un deep link callback
        handleWalletCallback(intent)
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
                Log.e(TAG, "Google Sign-In fallito con codice: ${e.statusCode}", e)
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

    /**
     * Connessione con un wallet Solana (ad esempio Phantom) per il login
     */
    private fun connectWithSolanaWallet() {
        Log.d(TAG, "Tentativo di connessione con Phantom per login")

        // Verifica se il wallet Phantom è installato
        if (isPhantomInstalled()) {
            Log.d(TAG, "Phantom è installato, avvio dell’app per login")
            launchPhantom()
        } else {
            Log.d(TAG, "Phantom non è installato, reindirizzamento al Play Store")
            // Mostra un messaggio e offri di installarlo
            Toast.makeText(
                this,
                "Phantom non è installato. Installalo per continuare.",
                Toast.LENGTH_LONG
            ).show()

            // Apri il Play Store (cambiato packageName da "com.phantom.app" a "app.phantom")
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=app.phantom")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                // Se il Play Store non fosse disponibile, apri il browser
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=app.phantom")
                    )
                )
            }
        }
    }

    // Verifica se Phantom è installato (package name corretto: "app.phantom")
    private fun isPhantomInstalled(): Boolean {
        val packageManager = packageManager
        return try {
            packageManager.getPackageInfo("app.phantom", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Avvia Phantom con un deep link per il login
    private fun launchPhantom() {
        try {
            val phantomUri = Uri.parse("phantom://nativesparksapp/connect?redirect=nativesparksapp://callback/wallet_login")
            val intent = Intent(Intent.ACTION_VIEW, phantomUri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            Log.d(TAG, "Avvio di Phantom con URI: $phantomUri")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'avvio di Phantom", e)
            Toast.makeText(
                this,
                "Errore nell'avvio di Phantom: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gestisce l'intent quando l'app viene riaperta dal wallet
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent chiamato con intent: ${intent.data}")
        setIntent(intent)
        handleWalletCallback(intent)
    }

    // Gestione callback dal wallet
    private fun handleWalletCallback(intent: Intent) {
        val data = intent.data

        // Esempio: nativesparksapp://callback/wallet_login?public_key=...
        if (data != null &&
            data.scheme == "nativesparksapp" &&
            data.host == "callback" &&
            data.path == "/wallet_login"
        ) {

            Log.d(TAG, "Callback dal wallet Solana (login): $data")

            // Leggiamo il parametro "public_key"
            val walletAddress = data.getQueryParameter("public_key")
                ?: "Sconosciuto_${System.currentTimeMillis()}"

            Log.d(TAG, "Indirizzo wallet ottenuto per login: $walletAddress")

            // Salva il flag e l'indirizzo nelle SharedPreferences
            val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .putBoolean(KEY_REGISTERED_VIA_WALLET, true)
                .putString(KEY_WALLET_ADDRESS, walletAddress)
                .apply()

            Toast.makeText(
                this,
                "Login tramite Wallet Solana effettuato. Address: $walletAddress",
                Toast.LENGTH_SHORT
            ).show()

            // Vai a GameLaunchActivity
            startActivity(Intent(this, GameLaunchActivity::class.java))
            finish()
        }
    }

    // Gestisce la callback anche nel metodo onResume
    override fun onResume() {
        super.onResume()
        val intent = intent
        if (intent != null) {
            handleWalletCallback(intent)
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
