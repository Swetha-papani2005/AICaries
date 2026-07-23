package com.aicaries.app

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvScore.text = "${item.score}% · ${item.riskLevel}"
        holder.tvDate.text = item.timestamp
        holder.itemView.setOnClickListener { onClick(item) }
    }
    override fun getItemCount() = items.size
}