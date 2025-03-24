package com.example.nativesparksapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class GameLaunchActivity : AppCompatActivity() {

    private val TAG = "GameLaunchActivity"
    private val gamePackageName = "com.UnityTechnologies.com.unity.template.urpblank"
    private val gameApkName = "SparksAR1.7.apk"
    private val PREFS_NAME = "SparksGamePrefs"
    private val KEY_INSTALLATION_STARTED = "installation_started"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)  // <-- Il layout con "imageHexPlay"

        // 1. Recupera la ImageView che funge da pulsante "Play"
        val imageHexPlay: ImageView = findViewById(R.id.imageHexPlay)

        // 2. Cliccando sul poligono
        imageHexPlay.setOnClickListener {
            Log.d(TAG, "Pulsante Play cliccato")
            if (isGameInstalled()) {
                Log.d(TAG, "Gioco già installato, avvio in corso...")
                launchGame()
            } else {
                Log.d(TAG, "Gioco non installato, verifico se l'installazione è già stata avviata")
                if (isInstallationStarted()) {
                    // Se l'installazione è già stata avviata ma non completata
                    Log.d(TAG, "Installazione già avviata in precedenza, mostro prompt")
                    showInstallationPrompt()
                } else {
                    // Prima installazione
                    Log.d(TAG, "Prima installazione, avvio il processo di installazione")
                    installGame()
                    // Segna che l'installazione è stata avviata
                    markInstallationStarted()
                    Log.d(TAG, "Installazione avviata e segnata nelle preferenze")
                }
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
            Log.d(TAG, "isGameInstalled: true - Il gioco è installato")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "isGameInstalled: false - Il gioco non è installato")
            false
        }
    }

    // Verifica se l'installazione è già stata avviata
    private fun isInstallationStarted(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val started = prefs.getBoolean(KEY_INSTALLATION_STARTED, false)
        Log.d(TAG, "isInstallationStarted: $started")
        return started
    }

    // Segna che l'installazione è stata avviata
    private fun markInstallationStarted() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_INSTALLATION_STARTED, true).apply()
        Log.d(TAG, "markInstallationStarted: Installazione segnata come avviata")
    }

    // Mostra un prompt per chiedere all'utente se vuole completare l'installazione
    private fun showInstallationPrompt() {
        AlertDialog.Builder(this)
            .setTitle("Installazione in corso")
            .setMessage("Sembra che l'installazione del gioco sia stata avviata ma non completata. Vuoi riprendere l'installazione?")
            .setPositiveButton("Sì") { _, _ ->
                Log.d(TAG, "Utente ha scelto di riprendere l'installazione")
                installGame()
            }
            .setNegativeButton("No") { _, _ ->
                Log.d(TAG, "Utente ha scelto di non riprendere l'installazione")
            }
            .show()
    }

    // Se il gioco non è installato, copia l'APK da assets e avvia l'installazione (con FileProvider)
    private fun installGame() {
        Log.d(TAG, "installGame: Inizio processo di installazione")
        val apkFile = copyApkFromAssets()
        if (apkFile != null) {
            Log.d(TAG, "APK copiato con successo: ${apkFile.absolutePath}")
            // Ottieni l'Uri sicuro tramite FileProvider
            val apkUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                apkFile
            )
            Log.d(TAG, "URI FileProvider ottenuto: $apkUri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                Log.d(TAG, "Avvio intent di installazione")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Errore nell'avvio dell'intent di installazione", e)
                showErrorDialog("Impossibile installare il gioco. Prova a installarlo manualmente.")
            }
        } else {
            Log.e(TAG, "Errore nella copia dell'APK")
            showErrorDialog("Errore durante la copia dell'APK.")
        }
    }

    // Copia il file APK dalla cartella assets in un percorso accessibile al sistema
    private fun copyApkFromAssets(): File? {
        val apkFile = File(getExternalFilesDir(null), gameApkName)
        // Se l'APK esiste già, non lo ricopiamo
        if (apkFile.exists()) {
            Log.d(TAG, "copyApkFromAssets: APK già esistente, non lo ricopio")
            return apkFile
        }

        Log.d(TAG, "copyApkFromAssets: APK non esistente, inizio copia")
        return try {
            assets.open(gameApkName).use { inputStream ->
                FileOutputStream(apkFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d(TAG, "copyApkFromAssets: Copia completata con successo")
            apkFile
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante la copia dell'APK", e)
            e.printStackTrace()
            null
        }
    }

    // Avvia il gioco installato
    private fun launchGame() {
        Log.d(TAG, "launchGame: Tentativo di avvio del gioco")
        val launchIntent = packageManager.getLaunchIntentForPackage(gamePackageName)
        if (launchIntent != null) {
            Log.d(TAG, "Intent di avvio ottenuto, avvio il gioco")
            startActivity(launchIntent)
        } else {
            Log.e(TAG, "Impossibile ottenere l'intent di avvio")
            showErrorDialog("Impossibile avviare il gioco.")
        }
    }

    // Mostra un semplice AlertDialog in caso di errore
    private fun showErrorDialog(message: String) {
        Log.e(TAG, "showErrorDialog: $message")
        AlertDialog.Builder(this)
            .setTitle("Errore")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
