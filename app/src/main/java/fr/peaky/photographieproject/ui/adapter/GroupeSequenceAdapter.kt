package fr.peaky.photographieproject.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.model.GroupeSequence
import fr.peaky.photographieproject.ui.activity.PelliculeDetailActivity
import fr.peaky.photographieproject.ui.activity.SequenceListActivity
import fr.peaky.photographieproject.ui.component.inflate
import kotlinx.android.synthetic.main.groupe_sequence_item_holder.view.*


const val GROUPE_SEQUENCE_EXTRA_KEY = "groupe_sequence_extra_key"

class GroupSequenceAdapter : RecyclerView.Adapter<GroupeSequenceViewHolder>() {

    lateinit var listener: (GroupeSequence) -> Unit
    private var groupeSequences = emptyList<GroupeSequence>()

    override fun getItemCount(): Int {
        return groupeSequences.size
    }

    override fun onBindViewHolder(holder: GroupeSequenceViewHolder, position: Int) {
        holder.bindPellicule(groupeSequences[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupeSequenceViewHolder {
        val inflatedView: View = parent.inflate(R.layout.groupe_sequence_item_holder, false)
        return GroupeSequenceViewHolder(inflatedView, listener)
    }

    fun updateGroupeSequenceList(pellicules: List<GroupeSequence>) {
        this.groupeSequences = pellicules
        notifyDataSetChanged()
    }

}

class GroupeSequenceViewHolder(view: View, listener: (GroupeSequence) -> Unit) : RecyclerView.ViewHolder(view) {

    private val rootView = view
    private var groupeSequence: GroupeSequence? = null

    init {
        rootView.setOnClickListener {
            val intent = Intent(it.context, SequenceListActivity::class.java)
            intent.putExtra(GROUPE_SEQUENCE_EXTRA_KEY, groupeSequence)
            it.context.startActivity(intent)
        }
        rootView.setOnLongClickListener{
            groupeSequence?.let { it1 -> listener(it1) }
            return@setOnLongClickListener true
        }
    }

    fun bindPellicule(groupeSequence: GroupeSequence) {
        this.groupeSequence = groupeSequence
        rootView.
        rootView.groupeSequenceName.text = groupeSequence.name
    }
}

class CustomGroupeSequenceScrollListener(pelliculeDetailActivity: PelliculeDetailActivity) :
    RecyclerView.OnScrollListener() {

    private val pelliculeDetailActivity = pelliculeDetailActivity

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        when {
            dy > 0 -> pelliculeDetailActivity.notifyPelliculeDetailMovingScroll(1)
            dy < 0 -> pelliculeDetailActivity.notifyPelliculeDetailMovingScroll(2)
            else -> pelliculeDetailActivity.notifyPelliculeDetailMovingScroll(0)
        }
    }
}
