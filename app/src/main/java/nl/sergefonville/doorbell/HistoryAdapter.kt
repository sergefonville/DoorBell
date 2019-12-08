package nl.sergefonville.doorbell

import android.R.id
import android.R.layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.view.*
import nl.sergefonville.doorbell.R
import java.util.ArrayList


class HistoryAdapter(private val history: ArrayList<String>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var mTextView: TextView

        init {
            mTextView = v.findViewById(R.id.row_text) as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {

        // Create View
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_row, parent, false)

        return ViewHolder(v)
    }

    fun add(data: String) {
        history.add(data)
        this.notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mTextView.text = history[position]
    }

    override fun getItemCount(): Int {
        return history.size
    }


}