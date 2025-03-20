package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
        Toast.makeText(this, "Registrazione con Google (da implementare)", Toast.LENGTH_SHORT).show()
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
}
