package com.example.nativesparksapp

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    // Firebase
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // SharedPreferences
    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_NAME = "key_name"
        private const val KEY_NICKNAME = "key_nickname"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_COUNTRY = "key_country"
        private const val KEY_GENDER = "key_gender"
        private const val KEY_ADDRESS = "key_address"
    }

    // View
    private lateinit var editFullName: EditText
    private lateinit var editNickname: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var spinnerGender: Spinner
    private lateinit var editAddress: EditText
    private lateinit var buttonSubmit: Button
    private var progressBar: ProgressBar? = null
    private var textError: TextView? = null
    private lateinit var iconBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        try {
            // 1. Associa le view
            editFullName   = findViewById(R.id.editFullName)
            editNickname   = findViewById(R.id.editNickName)
            editEmail      = findViewById(R.id.editEmail)
            editPhone      = findViewById(R.id.editPhoneNumber)
            spinnerCountry = findViewById(R.id.spinnerCountry)
            spinnerGender  = findViewById(R.id.spinnerGenre)
            editAddress    = findViewById(R.id.editAddress)
            buttonSubmit   = findViewById(R.id.buttonSubmit)
            progressBar    = findViewById(R.id.progressBar)
            textError      = findViewById(R.id.textError)
            iconBack       = findViewById(R.id.iconBack)

            // Gestione del pulsante indietro
            iconBack.setOnClickListener {
                onBackPressed()
            }

            val countries = arrayOf("USA", "Italy", "France", "Japan")
            spinnerCountry.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                countries
            )

            val genders = arrayOf("Male", "Female", "Other")
            spinnerGender.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genders
            )

            // 2. Carica i dati da SharedPreferences (cache locale)
            loadDataFromPrefs()

            // 3. Se l'utente è loggato, ottieni UID e carica i dati da Firestore
            val currentUser = auth.currentUser
            if (currentUser != null) {
                editEmail.isEnabled = false // disabilita se l'email non deve essere modificabile
                loadDataFromFirestore(currentUser.uid)
            } else {
                // Nessun utente loggato, magari chiudi l'activity o mostra un errore
                Toast.makeText(this, "Nessun utente loggato.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // 4. Gestisci il click su "Submit" per validare e salvare dati
            buttonSubmit.setOnClickListener {
                saveProfile()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Errore durante l'inizializzazione: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadDataFromPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editFullName.setText(prefs.getString(KEY_NAME, ""))
        editNickname.setText(prefs.getString(KEY_NICKNAME, ""))
        editEmail.setText(prefs.getString(KEY_EMAIL, ""))
        editPhone.setText(prefs.getString(KEY_PHONE, ""))
        // Per gli spinner, potresti fare un setSelection in base a un indice salvato
        // Oppure salvare la stringa e cercare l'indice corrispondente
        val savedCountry = prefs.getString(KEY_COUNTRY, "")
        setSpinnerSelection(spinnerCountry, savedCountry)

        val savedGender = prefs.getString(KEY_GENDER, "")
        setSpinnerSelection(spinnerGender, savedGender)

        editAddress.setText(prefs.getString(KEY_ADDRESS, ""))
    }

    private fun loadDataFromFirestore(uid: String) {
        progressBar?.visibility = View.VISIBLE

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                progressBar?.visibility = View.GONE
                if (document != null && document.exists()) {
                    // Leggi i campi
                    val name     = document.getString("name") ?: ""
                    val nickname = document.getString("nickname") ?: ""
                    val email    = document.getString("email") ?: ""
                    val phone    = document.getString("phone") ?: ""
                    val country  = document.getString("country") ?: ""
                    val gender   = document.getString("gender") ?: ""
                    val address  = document.getString("address") ?: ""

                    // Imposta nei campi
                    editFullName.setText(name)
                    editNickname.setText(nickname)
                    editEmail.setText(email)
                    editPhone.setText(phone)
                    setSpinnerSelection(spinnerCountry, country)
                    setSpinnerSelection(spinnerGender, gender)
                    editAddress.setText(address)

                    // Aggiorna la cache locale
                    saveDataToPrefs(name, nickname, email, phone, country, gender, address)
                }
            }
            .addOnFailureListener { e ->
                progressBar?.visibility = View.GONE
                Toast.makeText(this, "Errore caricamento dati: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        // Validazione campi obbligatori
        val name = editFullName.text.toString().trim()
        val nickname = editNickname.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val phone = editPhone.text.toString().trim()
        val country = spinnerCountry.selectedItem.toString()
        val gender = spinnerGender.selectedItem.toString()
        val address = editAddress.text.toString().trim()

        // Esempio: Non permetti nome/nickname/email vuoti
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(nickname) || TextUtils.isEmpty(email)) {
            showError("Compila i campi obbligatori (Nome, Nickname, Email).")
            return
        }

        progressBar?.visibility = View.VISIBLE
        textError?.visibility = View.GONE

        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Nessun utente loggato.")
            progressBar?.visibility = View.GONE
            return
        }

        val uid = currentUser.uid
        val userMap = mapOf(
            "name" to name,
            "nickname" to nickname,
            "email" to email,
            "phone" to phone,
            "country" to country,
            "gender" to gender,
            "address" to address
        )

        // Salva su Firestore
        firestore.collection("users").document(uid)
            .update(userMap)
            .addOnSuccessListener {
                progressBar?.visibility = View.GONE
                Toast.makeText(this, "Profilo aggiornato!", Toast.LENGTH_SHORT).show()

                // Aggiorna cache locale
                saveDataToPrefs(name, nickname, email, phone, country, gender, address)

                // Chiudi l'Activity dopo il salvataggio
                finish()
            }
            .addOnFailureListener { e ->
                progressBar?.visibility = View.GONE
                showError("Errore salvataggio dati: ${e.message}")
            }
    }

    private fun showError(message: String) {
        // Se hai un textError nel layout, usalo, altrimenti un toast
        textError?.text = message
        textError?.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveDataToPrefs(
        name: String, nickname: String, email: String, phone: String,
        country: String, gender: String, address: String
    ) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .putString(KEY_COUNTRY, country)
            .putString(KEY_GENDER, gender)
            .putString(KEY_ADDRESS, address)
            .apply()
    }

    // Helper per posizionare correttamente lo Spinner sul valore salvato
    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        if (value.isNullOrEmpty()) return
        val adapter = spinner.adapter ?: return
        for (i in 0 until adapter.count) {
            val item = adapter.getItem(i)
            if (item != null && item.toString().equals(value, ignoreCase = true)) {
                spinner.setSelection(i)
                break
            }
        }
    }
}
