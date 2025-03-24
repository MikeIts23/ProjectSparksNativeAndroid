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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Pulisci le SharedPreferences all'avvio della registrazione
        // per evitare che rimangano dati di utenti precedenti
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

        // Configura Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        // Pulisci eventuali altre SharedPreferences dell'app
        // Se ci sono altre SharedPreferences specifiche dell'utente, aggiungerle qui

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
