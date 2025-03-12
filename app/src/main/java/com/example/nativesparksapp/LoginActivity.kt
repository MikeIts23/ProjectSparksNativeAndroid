package com.example.nativesparksapp
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nativesparksapp.GameActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.androidgamesdk.GameActivity
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
    private lateinit var imageApple: ImageView

    private var isPasswordVisible = false

    // Firebase Auth
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Google Sign In Client
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        imageTogglePassword = findViewById(R.id.imageTogglePassword)
        textForgotPassword = findViewById(R.id.textForgotPassword)
        textErrorMessage = findViewById(R.id.textErrorMessage)
        buttonSignIn = findViewById(R.id.buttonSignIn)
        buttonRegister = findViewById(R.id.buttonRegister)
        imageGoogle = findViewById(R.id.imageGoogle)
        imageApple = findViewById(R.id.imageApple)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Imposta i listener
        imageTogglePassword.setOnClickListener { togglePasswordVisibility() }
        buttonSignIn.setOnClickListener { signIn() }
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        textForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password cliccato!", Toast.LENGTH_SHORT).show()
            // Aggiungi qui la logica per il reset password se necessario
        }
        imageGoogle.setOnClickListener { signInWithGoogle() }
        imageApple.setOnClickListener { signInWithApple() }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Usa l'icona standard per "occhio aperto"; puoi usare una risorsa di sistema o la tua
            imageTogglePassword.setImageResource(R.drawable.ic_eye_open)
        } else {
            editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageTogglePassword.setImageResource(R.drawable.ic_eye)
        }
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    // Login con email e password
    private fun signIn() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            showError("Per favore, compila tutti i campi.")
            return
        }

        textErrorMessage.visibility = View.GONE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showError(task.exception?.localizedMessage ?: "Errore sconosciuto durante il login.")
                }
            }
    }

    private fun showError(message: String) {
        textErrorMessage.visibility = View.VISIBLE
        textErrorMessage.text = message
    }

    // Login con Google
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
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showError(task.exception?.localizedMessage ?: "Google sign in failed")
                }
            }
    }

    // Placeholder per Apple Sign In
    private fun signInWithApple() {
        Toast.makeText(this, "Login con Apple non implementato.", Toast.LENGTH_SHORT).show()
    }
}
