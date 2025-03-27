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

class RegisterActivity : AppCompatActivity()  {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageTogglePassword: ImageView
    private lateinit var progressLoading: ProgressBar
    private lateinit var buttonSignUp: Button
    private lateinit var textErrorMessage: TextView

    private lateinit var imageGoogle: ImageView
    private lateinit var imageApple: ImageView

    // Pulsante/Immagine per il wallet
    private lateinit var imageWallet: ImageView

    private var isLoading = false
    private var isPasswordVisible = false

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Google Sign In Client
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    // Tag per il logging
    private val TAG = "RegisterActivity"

    // Chiave per memorizzare l'ID dell'ultimo utente loggato
    companion object {
        const val PREFS_USER_ID = "user_prefs"
        const val KEY_LAST_USER_ID = "last_user_id"

        // Key per indicare se l'utente si è registrato via wallet
        const val PREFS_NAME = "UserPrefs"
        const val KEY_REGISTERED_VIA_WALLET = "registered_via_wallet"
        const val KEY_WALLET_ADDRESS = "wallet_address"
    }

    // Istanza di web3j
    private lateinit var web3j: Web3j

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Pulisci le SharedPreferences all'avvio della registrazione
        clearAllUserPreferences()

        // Inizializza i riferimenti
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextName = findViewById(R.id.editTextName)
        editTextPassword = findViewById(R.id.editTextPassword)
        imageTogglePassword = findViewById(R.id.imageTogglePassword)
        progressLoading = findViewById(R.id.progressLoading)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        textErrorMessage = findViewById(R.id.textErrorMessage)
        imageGoogle = findViewById(R.id.imageGoogle)
        imageApple = findViewById(R.id.imageApple)
        imageWallet = findViewById(R.id.imageWallet)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/YOUR_INFURA_PROJECT_ID") )

        // Toggle password
        imageTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Click su "Sign up"
        buttonSignUp.setOnClickListener {
            onSignUp()
        }

        // Click su icona Google
        imageGoogle.setOnClickListener {
            onSignUpWithGoogle()
        }

        // Click su icona Apple
        imageApple.setOnClickListener {
            onSignUpWithApple()
        }

        // Click su icona/pulsante "Wallet"
        imageWallet.setOnClickListener {
            connectWithWallet()
        }

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
            imageTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
        }
        // Mantieni il cursore alla fine del testo
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    private fun onSignUp() {
        val email = editTextEmail.text.toString().trim()
        val name = editTextName.text.toString().trim()
        val password = editTextPassword.text.toString()

        // Mostra spinner e nascondi bottone
        setLoading(true)
        textErrorMessage.visibility = View.GONE

        // Controlli preliminari
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

        // Creazione utente con FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    // Aggiorna displayName con il nome inserito
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(name)
                        .build()

                    firebaseUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { _ ->
                            // Registrazione ok
                            setLoading(false)
                            Toast.makeText(
                                this,
                                "Registrazione completata!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Salva l'ID dell'utente corrente nelle SharedPreferences
                            firebaseUser?.uid?.let { userId ->
                                saveCurrentUserId(userId)
                                Log.d(TAG, "ID utente salvato: $userId")
                            }

                            // Vai alla GameLaunchActivity e chiudi la schermata di registrazione
                            startActivity(Intent(this, GameLaunchActivity::class.java))
                            finish()
                        }
                } else {
                    // Errore FirebaseAuth
                    val errorMessage = task.exception?.localizedMessage
                        ?: "Errore sconosciuto durante la registrazione."
                    showErrorMessage(errorMessage)
                    setLoading(false)
                }
            }
    }

    private fun onSignUpWithGoogle() {
        // Avvia il flusso di login Google
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Gestisci il risultato dell'Intent di Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showErrorMessage("Google sign in failed: ${e.localizedMessage}")
                setLoading(false)
            }
        }
    }

    // Completa l'autenticazione Firebase con Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        setLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login Google ok: vai a GameActivity
                    Toast.makeText(
                        this,
                        "Registrazione con Google completata!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, GameLaunchActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showErrorMessage(task.exception?.localizedMessage ?: "Google sign in failed")
                    setLoading(false)
                }
            }
    }

    private fun onSignUpWithApple() {
        Toast.makeText(this, "Registrazione con Apple (da implementare)", Toast.LENGTH_SHORT).show()
    }

    // Metodo di connessione al wallet MetaMask
    private fun connectWithWallet() {
        Log.d(TAG, "Tentativo di connessione con MetaMask")

        // Verifica se MetaMask è installato
        if (isMetaMaskInstalled()) {
            Log.d(TAG, "MetaMask è installato, avvio dell'app")
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
            val metamaskUri = Uri.parse("metamask://dapp/nativesparksapp://callback/wallet")
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
            data.host == "callback" && data.path == "/wallet") {

            Log.d(TAG, "Callback dal wallet ricevuta: $data")

            // Estrai l'indirizzo del wallet dai parametri (se disponibile)
            val walletAddress = data.getQueryParameter("address") ?: "0x" + generateRandomHexAddress()

            Log.d(TAG, "Indirizzo wallet ottenuto: $walletAddress")

            // Salva il flag e l'indirizzo nelle SharedPreferences
            val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .putBoolean(KEY_REGISTERED_VIA_WALLET, true)
                .putString(KEY_WALLET_ADDRESS, walletAddress)
                .apply()

            Toast.makeText(
                this,
                "Registrazione tramite Wallet completata. Address: $walletAddress",
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

    // Pulisce tutte le SharedPreferences dell'utente
    private fun clearAllUserPreferences() {
        Log.d(TAG, "Pulizia delle SharedPreferences all'avvio della registrazione")

        // Pulisci le SharedPreferences di ProfileActivity
        val profilePrefs = getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        profilePrefs.edit().clear().apply()

        // Pulisci le SharedPreferences di EditProfileActivity
        val editProfilePrefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        editProfilePrefs.edit().clear().apply()

        // Altre SharedPreferences se presenti...

        Log.d(TAG, "SharedPreferences pulite con successo all'avvio della registrazione")
    }

    // Salva l'ID dell'utente corrente nelle SharedPreferences
    private fun saveCurrentUserId(userId: String) {
        val userPrefs = getSharedPreferences(PREFS_USER_ID, Context.MODE_PRIVATE)
        userPrefs.edit()
            .putString(KEY_LAST_USER_ID, userId)
            .apply()
        Log.d(TAG, "ID utente salvato nelle SharedPreferences: $userId")
    }
}
