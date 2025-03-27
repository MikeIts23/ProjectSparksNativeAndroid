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

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageTogglePassword: ImageView
    private lateinit var progressLoading: ProgressBar
    private lateinit var buttonSignUp: Button
    private lateinit var textErrorMessage: TextView

    private lateinit var imageGoogle: ImageView
    private lateinit var imageApple: ImageView

    // (NUOVO) Pulsante/Immagine per il wallet (va aggiunto a layout se non esiste)
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

        // (NUOVO) Key per indicare se l'utente si è registrato via wallet
        const val PREFS_NAME = "UserPrefs"
        const val KEY_REGISTERED_VIA_WALLET = "registered_via_wallet"
    }

    // (NUOVO) Istanza di web3j
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


        web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/YOUR_INFURA_PROJECT_ID"))

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

        // (NUOVO) Click su icona/pulsante "Wallet"
        imageWallet.setOnClickListener {
            connectWithWallet()
        }
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

    // (NUOVO) Metodo di connessione al wallet
    private fun connectWithWallet() {
        // Qui inserisci la logica di connessione con il wallet:
        // - WalletConnect
        // - MetaMask
        // - Creazione di un account locale con web3j, etc.
        // Per esempio, recuperi un address e lo salvi
        val walletAddress = "0xYourWalletAddress"

        // Salva il flag nelle SharedPreferences
        val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_REGISTERED_VIA_WALLET, true).apply()

        Toast.makeText(
            this,
            "Registrazione tramite Wallet completata. Address: $walletAddress",
            Toast.LENGTH_SHORT
        ).show()

        // Vai a GameLaunchActivity
        startActivity(Intent(this, GameLaunchActivity::class.java))
        finish()
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
