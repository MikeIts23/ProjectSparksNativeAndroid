package com.example.nativesparksapp

import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore

class ContactsActivity : BaseActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var spinnerSubject: Spinner
    private lateinit var editMessage: EditText
    private lateinit var buttonSend: Button

    // Se vuoi salvare su Firestore
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        spinnerSubject = findViewById(R.id.spinnerSubject)
        editMessage = findViewById(R.id.editMessage)
        buttonSend = findViewById(R.id.buttonSend)

        // Icona back
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        iconBack.setOnClickListener { finish() }

        // ESEMPIO: Inizializzo a mano un ArrayAdapter se NON sto usando `android:entries`
        // Se invece hai già `android:entries="@array/contact_subject_array"` nel layout,
        // puoi saltare questa parte, e lo spinner visualizzerà i 3 item, mantenendo la scelta.
        val subjectList = listOf("Bug","Feedback","Question")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            subjectList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter

        // Se vuoi "vedere" la selezione in un Toast immediatamente
        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // L’utente ha appena scelto: subjectList[position]
                // Non serve far nulla se vuoi semplicemente che il testo rimanga
                // (è la default behaviour del spinner).
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Non fare nulla
            }
        }

        buttonSend.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val subject = spinnerSubject.selectedItem?.toString() ?: ""
            val message = editMessage.text.toString().trim()

            // ... Salva su Firestore, mostra toast, e rimani nella stessa pagina ...
            val dataMap = mapOf(
                "name" to name,
                "email" to email,
                "subject" to subject,
                "message" to message,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("supportRequests")
                .add(dataMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email sent ($subject)", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
