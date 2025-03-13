package com.example.nativesparksapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ContactsActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var spinnerSubject: Spinner
    private lateinit var editMessage: EditText
    private lateinit var buttonSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // Inizializzazione dei riferimenti ai View
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        spinnerSubject = findViewById(R.id.spinnerSubject)
        editMessage = findViewById(R.id.editMessage)
        buttonSend = findViewById(R.id.buttonSend)

        // Listener per icona back
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        iconBack.setOnClickListener {
            finish()  // Chiude l’Activity e torna indietro
        }

        buttonSend.setOnClickListener {
            // Recupera i dati dai campi:
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val subject = spinnerSubject.selectedItem.toString()
            val message = editMessage.text.toString().trim()

            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Solo app di posta
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@myapp.com")) // Cambia se serve
                putExtra(Intent.EXTRA_SUBJECT, "Richiesta da $name: $subject")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Mittente: $name\nEmail: $email\n\nMessaggio:\n$message"
                )
            }

            // Verifichiamo che esista un’app email
            if (mailIntent.resolveActivity(packageManager) != null) {
                startActivity(mailIntent)
            } else {
                Toast.makeText(this, "Nessuna app email trovata.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
