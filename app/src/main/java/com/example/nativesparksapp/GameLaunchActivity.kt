package com.example.nativesparksapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider   // <--- IMPORTANTE
import java.io.File
import java.io.FileOutputStream

class GameLaunchActivity : AppCompatActivity() {

    private val gamePackageName = "com.UnityTechnologies.com.unity.template.urpblank"
    private val gameApkName = "SparksAR1.7.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)  // <-- Il layout con "imageHexPlay"

        // 1. Recupera la ImageView che funge da pulsante "Play"
        val imageHexPlay: ImageView = findViewById(R.id.imageHexPlay)

        // 2. Cliccando sul poligono
        imageHexPlay.setOnClickListener {
            if (isGameInstalled()) {
                launchGame()
            } else {
                installGame()
            }
        }

        // ---- Aggiunta: gestione Bottom Navigation ----
        val btnPlay = findViewById<ImageButton>(R.id.btn_home)
        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        btnPlay.setOnClickListener {
            startActivity(Intent(this, GameLaunchActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        // ---------------------------------------------
    }

    // Verifica se il gioco è già installato sul dispositivo
    private fun isGameInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(gamePackageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Se il gioco non è installato, copia l'APK da assets e avvia l'installazione (con FileProvider)
    private fun installGame() {
        val apkFile = copyApkFromAssets()
        if (apkFile != null) {
            // Ottieni l'Uri sicuro tramite FileProvider
            val apkUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                showErrorDialog("Impossibile installare il gioco. Prova a installarlo manualmente.")
            }
        } else {
            showErrorDialog("Errore durante la copia dell'APK.")
        }
    }

    // Copia il file APK dalla cartella assets in un percorso accessibile al sistema
    private fun copyApkFromAssets(): File? {
        val apkFile = File(getExternalFilesDir(null), gameApkName)
        // Se l’APK esiste già, non lo ricopiamo
        if (apkFile.exists()) return apkFile

        return try {
            assets.open(gameApkName).use { inputStream ->
                FileOutputStream(apkFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Avvia il gioco installato
    private fun launchGame() {
        val launchIntent = packageManager.getLaunchIntentForPackage(gamePackageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            showErrorDialog("Impossibile avviare il gioco.")
        }
    }

    // Mostra un semplice AlertDialog in caso di errore
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Errore")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
