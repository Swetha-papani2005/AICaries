package com.aicaries.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_USER = 1
        const val VIEW_BOT = 2
    }

    override fun getItemViewType(position: Int) =
        if (messages[position].isUser) VIEW_USER else VIEW_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is UserViewHolder) holder.bind(msg)
        else if (holder is BotViewHolder) holder.bind(msg)
    }

    override fun getItemCount() = messages.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        fun bind(msg: ChatMessage) { tvMessage.text = msg.text }
    }

    class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        fun bind(msg: ChatMessage) { tvMessage.text = msg.text }
    }
}

class ChatActivity : AppCompatActivity() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var layoutTyping: LinearLayout
    private val history = mutableListOf<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        layoutTyping = findViewById(R.id.layoutTyping)

        adapter = ChatAdapter(messages)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        findViewById<CardView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<CardView>(R.id.btnSend).setOnClickListener { sendMessage() }

        addBotMessage("Hi there! 🦷 I'm Toothie, your AI dental assistant! Ask me anything about dental health, general health, or any other topic!")
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        etMessage.setText("")
        addUserMessage(text)
        showTyping(true)

        val params = JSONObject().apply {
            put("message", text)
            put("history", JSONArray()) // simplified history for now
        }

        ApiClient.post("chat.php", params) { response ->
            runOnUiThread {
                showTyping(false)
                if (response != null && response.optBoolean("success")) {
                    val reply = response.optJSONObject("data")?.optString("reply")
                        ?: "Sorry, I couldn't respond."
                    addBotMessage(reply)
                    history.add(mapOf("role" to "user", "text" to text))
                    history.add(mapOf("role" to "model", "text" to reply))
                } else {
                    val errorMsg = response?.optString("message") ?: "null response — connection failed"
                    addBotMessage("⚠️ Error: $errorMsg")
                }
            }
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(ChatMessage(text, false))
        adapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun showTyping(show: Boolean) {
        layoutTyping.visibility = if (show) View.VISIBLE else View.GONE
    }
}