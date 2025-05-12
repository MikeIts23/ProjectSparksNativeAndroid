package com.example.nativesparksapp

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class LeaderboardActivity : BaseActivity() {

    data class LeaderboardEntry(
        val displayName: String = "",
        val score: Int = 0
    )

    private lateinit var indicator: View
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: LeaderboardAdapter

    // Tab state: "friends", "national", "global"
    private var currentTab = "friends"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        BottomNavigationHelper.setupBottomNavigation(this)
        indicator = findViewById(R.id.viewIndicator)

        // Inizializza RecyclerView
        recycler = findViewById(R.id.recyclerLeaderboard)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter()
        recycler.adapter = adapter

        initTabClickListeners()

        // Carica subito la classifica
        loadLeaderboard()
    }

    private fun initTabClickListeners() {
        val tabFriends = findViewById<FrameLayout>(R.id.tabFriends)
        val tabNational = findViewById<FrameLayout>(R.id.tabNational)
        val tabGlobal = findViewById<FrameLayout>(R.id.tabGlobal)

        tabFriends.setOnClickListener {
            currentTab = "friends"
            animateIndicatorTo(tabFriends)
            loadLeaderboard()
        }
        tabNational.setOnClickListener {
            currentTab = "national"
            animateIndicatorTo(tabNational)
            loadLeaderboard()
        }
        tabGlobal.setOnClickListener {
            currentTab = "global"
            animateIndicatorTo(tabGlobal)
            loadLeaderboard()
        }
    }

    private fun animateIndicatorTo(tab: View) {
        indicator.animate()
            .x(tab.x)
            .setDuration(300)
            .withStartAction {
                val params = indicator.layoutParams
                params.width = tab.width
                indicator.layoutParams = params
            }
            .start()
    }

    private fun loadLeaderboard() {
        // Sostituibile con filtro per currentTab: per ora usiamo la stessa collection
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val entries = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<LeaderboardEntry>()
                }

                // Prime 3
                val top3 = entries.take(3)
                updatePodium(top3)

                // Resto
                val others = entries.drop(3)
                adapter.submitList(others.map { LeaderboardItem(it.displayName, it.score) })
            }
            .addOnFailureListener { e ->
                // Qui potresti mostrare un Toast di errore
            }
    }

    private fun updatePodium(top3: List<LeaderboardEntry>) {
        if (top3.size < 3) return

        findViewById<TextView>(R.id.textFirstName).text  = top3[0].displayName
        findViewById<TextView>(R.id.textFirstScore).text = top3[0].score.toString()

        findViewById<TextView>(R.id.textSecondName).text  = top3[1].displayName
        findViewById<TextView>(R.id.textSecondScore).text = top3[1].score.toString()

        findViewById<TextView>(R.id.textThirdName).text  = top3[2].displayName
        findViewById<TextView>(R.id.textThirdScore).text = top3[2].score.toString()
    }
}
