package com.aicaries.app

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TipAdapter(private val tips: List<Tip>) : RecyclerView.Adapter<TipAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvDescription)
        val tvIcon: TextView = view.findViewById(R.id.tvIcon)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvTitle.text = tips[position].title
        holder.tvDesc.text = tips[position].description
    }
    override fun getItemCount() = tips.size
}