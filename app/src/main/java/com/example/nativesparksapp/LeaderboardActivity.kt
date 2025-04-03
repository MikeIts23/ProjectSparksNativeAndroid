package com.example.nativesparksapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LeaderboardActivity : AppCompatActivity() {

    // 1) Definisci la data class a livello di classe (ma fuori dalle funzioni)
    data class LeaderboardItem(val name: String, val score: Int)

    // Handler per l'aggiornamento periodico (5 minuti)
    private val refreshHandler = Handler(Looper.getMainLooper())
    private lateinit var refreshRunnable: Runnable

    // "friends", "national" o "global"
    private var currentTab = "friends"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Inizializza la bottom nav
        BottomNavigationHelper.setupBottomNavigation(this)

        // Listener per i tab
        initTabClickListeners()

        // Genera la classifica random subito
        generateRandomLeaderboard()

        // Timer ogni 5 minuti
        refreshRunnable = object : Runnable {
            override fun run() {
                generateRandomLeaderboard()
                refreshHandler.postDelayed(this, 5 * 60 * 1000)
            }
        }
        refreshHandler.postDelayed(refreshRunnable, 5 * 60 * 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Rimuove i callback quando Activity distrutta
        refreshHandler.removeCallbacksAndMessages(null)
    }

    /**
     * Imposta i listener per i tab (Friends, National, Global).
     */
    private fun initTabClickListeners() {
        val tabFriends = findViewById<FrameLayout>(R.id.tabFriends)
        val tabNational = findViewById<FrameLayout>(R.id.tabNational)
        val tabGlobal = findViewById<FrameLayout>(R.id.tabGlobal)

        tabFriends.setOnClickListener {
            currentTab = "friends"
            generateRandomLeaderboard()
        }
        tabNational.setOnClickListener {
            currentTab = "national"
            generateRandomLeaderboard()
        }
        tabGlobal.setOnClickListener {
            currentTab = "global"
            generateRandomLeaderboard()
        }
    }

    /**
     * Genera 10 nomi/punteggi random e ordina disc.
     * Podio = primi 3, resto = 7.
     */
    private fun generateRandomLeaderboard() {
        val possibleNames = when (currentTab) {
            "friends" -> listOf(
                "Mario Rossi", "Carlo Bianchi", "Paolo Gialli",
                "Elena Verdi", "Francesco Neri", "Lucia Fumagalli",
                "Roberto Franco", "Martina Zanetti", "Davide Colori",
                "Sergio Ferro", "Arianna Conti", "Marco Longo",
                "Chiara Strada", "Giulia Fazio", "Vincenzo Blu"
            )
            "national" -> listOf(
                "Romolo De Luca", "Federico S.", "Paola Q.",
                "Gianluca T.", "Alice K", "Luigi Ricci",
                "Serena Tosi", "Damiano Fedele", "Sofia Valli",
                "Antonio C.", "Camilla Rossa", "Martino G.",
                "Giorgia Pian", "Edoardo Santi", "Roby Russo"
            )
            else -> listOf(
                "Lucas White", "Kevin Chen", "Anna Johnson",
                "Peter Cook", "Jessica Thompson", "Nadia Silva",
                "Juan Pérez", "Isabelle Cooke", "Carlos Herrera",
                "Matias Alvarez", "Samantha G.", "Olivia Hall",
                "Ethan Parker", "Robert Brown", "Lily Wilson"
            )
        }

        // Pesca 10 nomi random
        val finalListSize = 10
        val randomNames = possibleNames.shuffled().take(finalListSize)

        // Genera punteggi 1000..7000 e crea una lista LeaderboardItem
        val userList = randomNames.map { nome ->
            LeaderboardItem(nome, Random.nextInt(1000, 7001))
        }.toMutableList()

        // Ordina disc
        userList.sortByDescending { it.score }

        // top3 + altri
        val top3 = userList.take(3)
        val others = userList.drop(3)

        // Aggiorna UI
        updatePodium(top3)
        updateList(others)
    }

    /**
     * Aggiorna i 3 podisti
     */
    private fun updatePodium(top3: List<LeaderboardItem>) {
        if (top3.size < 3) return

        val first = top3[0]
        val second = top3[1]
        val third = top3[2]

        // 1°
        findViewById<TextView>(R.id.textFirstName).text = first.name
        findViewById<TextView>(R.id.textFirstScore).text = first.score.toString()

        // 2°
        findViewById<TextView>(R.id.textSecondName).text = second.name
        findViewById<TextView>(R.id.textSecondScore).text = second.score.toString()

        // 3°
        findViewById<TextView>(R.id.textThirdName).text = third.name
        findViewById<TextView>(R.id.textThirdScore).text = third.score.toString()
    }

    /**
     * Aggiorna i 10 slot con ID unici.
     */
    private fun updateList(others: List<LeaderboardItem>) {
        // Definiamo una data class di "View" per ogni slot
        data class ItemViews(
            val image: ImageView,
            val name: TextView,
            val handle: TextView,
            val score: TextView
        )

        // Mappiamo i 10 slot in activity_leaderboard.xml
        val itemsMapping = listOf(
            ItemViews(
                findViewById(R.id.slot1Image),
                findViewById(R.id.textSlot1Name),
                findViewById(R.id.textSlot1Handle),
                findViewById(R.id.textSlot1Score)
            ),
            ItemViews(
                findViewById(R.id.slot2Image),
                findViewById(R.id.textSlot2Name),
                findViewById(R.id.textSlot2Handle),
                findViewById(R.id.textSlot2Score)
            ),
            ItemViews(
                findViewById(R.id.slot3Image),
                findViewById(R.id.textSlot3Name),
                findViewById(R.id.textSlot3Handle),
                findViewById(R.id.textSlot3Score)
            ),
            ItemViews(
                findViewById(R.id.slot4Image),
                findViewById(R.id.textSlot4Name),
                findViewById(R.id.textSlot4Handle),
                findViewById(R.id.textSlot4Score)
            ),
            ItemViews(
                findViewById(R.id.slot5Image),
                findViewById(R.id.textSlot5Name),
                findViewById(R.id.textSlot5Handle),
                findViewById(R.id.textSlot5Score)
            ),
            ItemViews(
                findViewById(R.id.slot6Image),
                findViewById(R.id.textSlot6Name),
                findViewById(R.id.textSlot6Handle),
                findViewById(R.id.textSlot6Score)
            ),
            ItemViews(
                findViewById(R.id.slot7Image),
                findViewById(R.id.textSlot7Name),
                findViewById(R.id.textSlot7Handle),
                findViewById(R.id.textSlot7Score)
            ),
            ItemViews(
                findViewById(R.id.slot8Image),
                findViewById(R.id.textSlot8Name),
                findViewById(R.id.textSlot8Handle),
                findViewById(R.id.textSlot8Score)
            ),
            ItemViews(
                findViewById(R.id.slot9Image),
                findViewById(R.id.textSlot9Name),
                findViewById(R.id.textSlot9Handle),
                findViewById(R.id.textSlot9Score)
            ),
            ItemViews(
                findViewById(R.id.slot10Image),
                findViewById(R.id.textSlot10Name),
                findViewById(R.id.textSlot10Handle),
                findViewById(R.id.textSlot10Score)
            )
        )

        // 'others' conterra 7 item (se la top3 è 3)
        // Cicliamo 10 slot
        for (i in 0 until 10) {
            val slotViews = itemsMapping[i]
            if (i < others.size) {
                val user = others[i]
                // Nome e punteggio
                slotViews.name.text = user.name
                slotViews.score.text = user.score.toString()

                // Creiamo handle fittizio
                val splitted = user.name.split(" ")
                val handleName = splitted.joinToString(separator = "") { it.lowercase() }
                slotViews.handle.text = "@$handleName"

                // Se vuoi cambiare anche l'immagine, potresti selezionarne una a caso
                slotViews.image.visibility = View.VISIBLE
            } else {
                // Non ci sono più utenti in 'others': pulisci
                slotViews.name.text = ""
                slotViews.handle.text = ""
                slotViews.score.text = ""
                slotViews.image.visibility = View.INVISIBLE
            }
        }
    }
}
