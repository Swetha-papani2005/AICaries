package com.aicaries.app

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MoreFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AI Chat button
        view.findViewById<CardView>(R.id.cardChat).setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        // Tips list
        val tips = listOf(
            Tip("🪥 Brush twice daily","Use fluoride toothpaste, soft-bristle brush, 2 minutes."),
            Tip("💧 Floss every day","Removes plaque between teeth where brush cannot reach."),
            Tip("🍬 Cut down on sugar","Limit soft drinks and candy. Rinse with water after sweets."),
            Tip("🏥 See a dentist regularly","A check-up every 6 months catches problems early."),
            Tip("💧 Drink more water","Fluoridated water helps wash away food and strengthens enamel."),
            Tip("🌿 Use mouthwash","Antibacterial mouthwash kills bacteria brushing misses."),
            Tip("🚭 Avoid tobacco","Tobacco greatly increases risk of gum disease and oral cancer.")
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvTips)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = TipAdapter(tips)
    }
}

data class Tip(val title: String, val description: String)