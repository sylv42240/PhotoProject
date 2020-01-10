package fr.peaky.photographieproject.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.model.Sequence
import fr.peaky.photographieproject.ui.activity.SequenceDetailActivity
import fr.peaky.photographieproject.ui.activity.SequenceListActivity
import fr.peaky.photographieproject.ui.component.inflate
import kotlinx.android.synthetic.main.sequence_item_holder.view.*


const val SEQUENCE_EXTRA_KEY = "sequence_extra_key"

class SequenceAdapter : RecyclerView.Adapter<SequenceViewHolder>() {

    lateinit var listener: (Sequence) -> Unit
    private var sequences = emptyList<Sequence>()

    override fun getItemCount(): Int {
        return sequences.size
    }

    override fun onBindViewHolder(holder: SequenceViewHolder, position: Int) {
        holder.bindPellicule(sequences[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SequenceViewHolder {
        val inflatedView: View = parent.inflate(R.layout.sequence_item_holder, false)
        return SequenceViewHolder(inflatedView, listener)
    }

    fun updateGroupeSequenceList(sequence: List<Sequence>) {
        this.sequences = sequence
        notifyDataSetChanged()
    }

}

class SequenceViewHolder(view: View, listener: (Sequence) -> Unit) : RecyclerView.ViewHolder(view) {

    private val rootView = view
    private var sequence: Sequence? = null

    init {
        rootView.setOnClickListener {
            val intent = Intent(it.context, SequenceDetailActivity::class.java)
            intent.putExtra(SEQUENCE_EXTRA_KEY, sequence)
            it.context.startActivity(intent)
        }
        rootView.setOnLongClickListener{
            sequence?.let { it1 -> listener(it1) }
            return@setOnLongClickListener true
        }
    }

    fun bindPellicule(sequence: Sequence) {
        this.sequence = sequence
        rootView.sequence_name.text = sequence.name
    }
}

class CustomSequenceScrollListener(sequenceListActivity: SequenceListActivity) :
    RecyclerView.OnScrollListener() {

    private val sequenceListActivity = sequenceListActivity

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        when {
            dy > 0 -> sequenceListActivity.notifySequenceListMovingScroll(1)
            dy < 0 -> sequenceListActivity.notifySequenceListMovingScroll(2)
            else -> sequenceListActivity.notifySequenceListMovingScroll(0)
        }
    }
}
