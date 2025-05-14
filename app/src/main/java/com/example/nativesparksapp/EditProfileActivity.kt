package com.example.nativesparksapp

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : BaseActivity() {

    // Firebase
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // SharedPreferences
    companion object {
        const val PREFS_NAME = "UserPrefs"
        private const val KEY_NAME     = "key_name"
        private const val KEY_NICKNAME = "key_nickname"
        private const val KEY_EMAIL    = "key_email"
        private const val KEY_PHONE    = "key_phone"
        private const val KEY_COUNTRY  = "key_country"
        private const val KEY_GENDER   = "key_gender"
        private const val KEY_ADDRESS  = "key_address"
    }

    // Views
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

        // Bind views
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

        // Back button
        iconBack.setOnClickListener { onBackPressed() }

        // Country spinner setup
        val countries = arrayOf(
            "United States","China","Japan","Germany","India",
            "United Kingdom","France","Italy","Canada","Russia",
            "South Korea","Brazil","Australia","Spain","Mexico",
            "Indonesia","Netherlands","Saudi Arabia","Turkey","Switzerland"
        )
        val countryAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            countries
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                return view
            }
        }
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = countryAdapter

        // Gender spinner setup
        val genders = arrayOf("Male","Female","Other")
        val genderAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            genders
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                return view
            }
        }
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        // Load cached prefs
        loadDataFromPrefs()

        // If logged in, load from Firestore
        val currentUser = auth.currentUser
        if (currentUser != null) {
            editEmail.isEnabled = true
            loadDataFromFirestore(currentUser.uid)
        } else {
            Toast.makeText(this, "Nessun utente loggato.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Submit button
        buttonSubmit.setOnClickListener { saveProfile() }
    }

    private fun loadDataFromPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editFullName.setText(prefs.getString(KEY_NAME, ""))
        editNickname.setText(prefs.getString(KEY_NICKNAME, ""))
        editEmail.setText(prefs.getString(KEY_EMAIL, ""))
        editPhone.setText(prefs.getString(KEY_PHONE, ""))
        setSpinnerSelection(spinnerCountry, prefs.getString(KEY_COUNTRY, ""))
        setSpinnerSelection(spinnerGender, prefs.getString(KEY_GENDER, ""))
        editAddress.setText(prefs.getString(KEY_ADDRESS, ""))
    }

    private fun loadDataFromFirestore(uid: String) {
        progressBar?.visibility = View.VISIBLE
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                progressBar?.visibility = View.GONE
                if (doc.exists()) {
                    editFullName.setText(doc.getString("name") ?: "")
                    editNickname.setText(doc.getString("nickname") ?: "")
                    editEmail.setText(doc.getString("email") ?: "")
                    editPhone.setText(doc.getString("phone") ?: "")
                    setSpinnerSelection(spinnerCountry, doc.getString("country"))
                    setSpinnerSelection(spinnerGender, doc.getString("gender"))
                    editAddress.setText(doc.getString("address") ?: "")
                    saveDataToPrefs(
                        doc.getString("name") ?: "",
                        doc.getString("nickname") ?: "",
                        doc.getString("email") ?: "",
                        doc.getString("phone") ?: "",
                        doc.getString("country") ?: "",
                        doc.getString("gender") ?: "",
                        doc.getString("address") ?: ""
                    )
                }
            }
            .addOnFailureListener { e ->
                progressBar?.visibility = View.GONE
                Toast.makeText(this, "Errore caricamento dati: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val name     = editFullName.text.toString().trim()
        val nickname = editNickname.text.toString().trim()
        val email    = editEmail.text.toString().trim()
        val phone    = editPhone.text.toString().trim()
        val country  = spinnerCountry.selectedItem.toString()
        val gender   = spinnerGender.selectedItem.toString()
        val address  = editAddress.text.toString().trim()

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(nickname) || TextUtils.isEmpty(email)) {
            showError("Compila i campi obbligatori (Nome, Nickname, Email).")
            return
        }

        progressBar?.visibility = View.VISIBLE
        textError?.visibility = View.GONE

        val currentUser = auth.currentUser ?: run {
            showError("Nessun utente loggato.")
            progressBar?.visibility = View.GONE
            return
        }

        val uid = currentUser.uid
        val userMap = mapOf(
            "name"     to name,
            "nickname" to nickname,
            "email"    to email,
            "phone"    to phone,
            "country"  to country,
            "gender"   to gender,
            "address"  to address
        )

        // 1) aggiorna /users/{uid}
        firestore.collection("users").document(uid)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                // Propaga su leaderboard
                firestore.collection("leaderboard").document(uid)
                    .set(mapOf("displayName" to nickname), SetOptions.merge())
                    .addOnSuccessListener {
                        // opzionale: log
                    }
                    .addOnFailureListener { e ->
                        // opzionale: gestisci errore
                    }

                progressBar?.visibility = View.GONE
                Toast.makeText(this, "Profilo aggiornato!", Toast.LENGTH_SHORT).show()
                saveDataToPrefs(name, nickname, email, phone, country, gender, address)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar?.visibility = View.GONE
                showError("Errore salvataggio dati: ${e.message}")
            }
    }

    private fun showError(message: String) {
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

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        if (value.isNullOrEmpty()) return
        val adapter = spinner.adapter ?: return
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(value, true)) {
                spinner.setSelection(i)
                break
            }
        }
    }
}
