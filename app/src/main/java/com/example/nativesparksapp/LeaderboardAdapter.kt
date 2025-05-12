package com.example.nativesparksapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class LeaderboardItem(val name: String, val score: Int)

class LeaderboardAdapter
    : ListAdapter<LeaderboardItem, LeaderboardAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<LeaderboardItem>() {
            override fun areItemsTheSame(a: LeaderboardItem, b: LeaderboardItem) =
                a.name == b.name
            override fun areContentsTheSame(a: LeaderboardItem, b: LeaderboardItem) =
                a == b
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img     = view.findViewById<ImageView>(R.id.imageAvatar)
        private val nameTv  = view.findViewById<TextView>(R.id.textName)
        private val scoreTv = view.findViewById<TextView>(R.id.textScore)

        fun bind(item: LeaderboardItem) {
            nameTv.text  = item.name
            scoreTv.text = item.score.toString()
            img.setImageResource(R.drawable.logosparks3)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))
}
