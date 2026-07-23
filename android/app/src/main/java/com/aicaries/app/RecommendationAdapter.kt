package com.aicaries.app

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecommendationAdapter(private val items: List<String>) : RecyclerView.Adapter<RecommendationAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvNumber)
        val tvText: TextView = view.findViewById(R.id.tvText)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_recommendation, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvNumber.text = "${position + 1}"
        holder.tvText.text = items[position]
    }
    override fun getItemCount() = items.size
}