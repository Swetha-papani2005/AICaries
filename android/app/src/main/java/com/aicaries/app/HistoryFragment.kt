package com.aicaries.app

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val rv = view.findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(requireContext())

        loadHistory(rv, session)

        view.findViewById<android.widget.ImageView>(R.id.btnDelete).setOnClickListener {
            val params = JSONObject()
            params.put("user_id", session.getUserId())
            ApiClient.post("delete_results.php", params) { response ->
                requireActivity().runOnUiThread {
                    if (response != null && response.getBoolean("success")) {
                        rv.adapter = HistoryAdapter(emptyList()) {}
                        Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadHistory(rv: RecyclerView, session: SessionManager) {
        val params = JSONObject()
        params.put("user_id", session.getUserId())

        ApiClient.post("get_results.php", params) { response ->
            requireActivity().runOnUiThread {
                if (response != null && response.getBoolean("success")) {
                    val data = response.getJSONArray("data")
                    val list = mutableListOf<HistoryItem>()
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        list.add(HistoryItem(
                            id        = obj.getInt("id"),
                            score     = obj.getInt("overall_score"),
                            riskLevel = obj.getString("risk_level"),
                            timestamp = obj.getString("created_at")
                        ))
                    }
                    rv.adapter = HistoryAdapter(list) { item ->
                        val intent = Intent(requireContext(), ResultDetailActivity::class.java)
                        intent.putExtra("result_id", item.id)
                        intent.putExtra("score",     item.score)
                        intent.putExtra("risk_level", item.riskLevel)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

data class HistoryItem(val id: Int, val score: Int, val riskLevel: String, val timestamp: String)