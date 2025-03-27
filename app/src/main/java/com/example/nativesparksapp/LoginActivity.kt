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
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
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
    private lateinit var imageApple: ImageView
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

    private lateinit var web3j: Web3j
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
        imageApple = findViewById(R.id.imageApple)
        progressLoading = findViewById(R.id.progressLoading)
        imageWallet = findViewById(R.id.imageWallet)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/YOUR_INFURA_PROJECT_ID") )

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

        imageWallet.setOnClickListener { connectWithWallet() }

        // Gestisci l'intent se l'activity è stata avviata da una callback
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

    private fun signInWithApple() {
        Toast.makeText(this, "Login con Apple non implementato.", Toast.LENGTH_SHORT).show()
    }

    // Metodo di connessione al wallet MetaMask
    private fun connectWithWallet() {
        Log.d(TAG, "Tentativo di connessione con MetaMask per login")

        // Verifica se MetaMask è installato
        if (isMetaMaskInstalled()) {
            Log.d(TAG, "MetaMask è installato, avvio dell'app per login")
            launchMetaMask()
        } else {
            Log.d(TAG, "MetaMask non è installato, reindirizzamento al Play Store")
            // MetaMask non è installato, mostra un messaggio e offri di installarlo
            Toast.makeText(
                this,
                "MetaMask non è installato. Installalo per continuare.",
                Toast.LENGTH_LONG
            ).show()

            // Apri il Play Store per installare MetaMask
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=io.metamask")))
            } catch (e: ActivityNotFoundException) {
                // Play Store non è disponibile, apri il browser
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=io.metamask") ))
            }
        }
    }

    // Verifica se MetaMask è installato
    private fun isMetaMaskInstalled(): Boolean {
        val packageManager = packageManager
        return try {
            packageManager.getPackageInfo("io.metamask", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Avvia MetaMask con la richiesta di connessione
    private fun launchMetaMask() {
        try {
            // Crea un deep link per MetaMask con callback alla nostra app
            // Usa un path diverso per distinguere il login dalla registrazione
            val metamaskUri = Uri.parse("metamask://dapp/nativesparksapp://callback/wallet_login")
            val intent = Intent(Intent.ACTION_VIEW, metamaskUri)

            // Imposta il flag per avviare una nuova attività
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            Log.d(TAG, "Avvio di MetaMask con URI: $metamaskUri")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'avvio di MetaMask", e)
            Toast.makeText(
                this,
                "Errore nell'avvio di MetaMask: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gestisce l'intent quando l'app viene riaperta tramite callback
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent chiamato con intent: ${intent.data}")

        // Imposta l'intent corrente
        setIntent(intent)

        // Gestisci l'intent se proviene dalla callback del wallet
        handleWalletCallback(intent)
    }

    // Gestisce la callback dal wallet
    private fun handleWalletCallback(intent: Intent) {
        val data = intent.data

        if (data != null && data.scheme == "nativesparksapp" &&
            data.host == "callback" && data.path == "/wallet_login") {

            Log.d(TAG, "Callback dal wallet ricevuta per login: $data")

            // Estrai l'indirizzo del wallet dai parametri (se disponibile)
            val walletAddress = data.getQueryParameter("address") ?: "0x" + generateRandomHexAddress()

            Log.d(TAG, "Indirizzo wallet ottenuto per login: $walletAddress")

            // Salva il flag e l'indirizzo nelle SharedPreferences
            val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .putBoolean(KEY_REGISTERED_VIA_WALLET, true)
                .putString(KEY_WALLET_ADDRESS, walletAddress)
                .apply()

            Toast.makeText(
                this,
                "Login tramite Wallet effettuato. Address: $walletAddress",
                Toast.LENGTH_SHORT
            ).show()

            // Vai a GameLaunchActivity
            startActivity(Intent(this, GameLaunchActivity::class.java))
            finish()
        }
    }

    // Genera un indirizzo esadecimale casuale (solo per demo)
    private fun generateRandomHexAddress(): String {
        val chars = "0123456789abcdef"
        val sb = StringBuilder(40)
        for (i in 0 until 40) {
            val index = (chars.length * Math.random()).toInt()
            sb.append(chars[index])
        }
        return sb.toString()
    }

    // Gestisce anche la callback nel metodo onResume
    override fun onResume() {
        super.onResume()

        // Controlla se c'è un intent pendente
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
