package fr.peaky.photographieproject.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.model.GroupeSequence
import fr.peaky.photographieproject.ui.activity.PelliculeDetailActivity
import fr.peaky.photographieproject.ui.component.inflate
import kotlinx.android.synthetic.main.groupe_sequence_item_holder.view.*
import kotlinx.android.synthetic.main.pellicule_item_holder.view.*


class GroupSequenceAdapter : RecyclerView.Adapter<GroupeSequenceViewHolder>() {

    private var groupeSequences = emptyList<GroupeSequence>()

    override fun getItemCount(): Int {
        return groupeSequences.size
    }

    override fun onBindViewHolder(holder: GroupeSequenceViewHolder, position: Int) {
        holder.bindPellicule(groupeSequences[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupeSequenceViewHolder {
        val inflatedView: View = parent.inflate(R.layout.groupe_sequence_item_holder, false)
        return GroupeSequenceViewHolder(inflatedView)
    }

    fun updatePelliculeList(pellicules: List<GroupeSequence>) {
        this.groupeSequences = pellicules
        notifyDataSetChanged()
    }

}

class GroupeSequenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val rootView = view
    private var groupeSequence: GroupeSequence? = null

    init {
        rootView.setOnClickListener {
            val intent = Intent(it.context, PelliculeDetailActivity::class.java)
            intent.putExtra(PELLICULE_EXTRA_KEY, groupeSequence)
            it.context.startActivity(intent)
        }
        rootView.setOnLongClickListener{
            deleteGroupeSequenceFireStore(groupeSequence?.id)
        }
    }

    private fun deleteGroupeSequenceFireStore(id: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun bindPellicule(groupeSequence: GroupeSequence) {
        this.groupeSequence = groupeSequence
        rootView.
        rootView.groupeSequenceName.text = groupeSequence.name
    }

    companion object {
        const val PELLICULE_EXTRA_KEY = "pellicule_extra_key"
    }
}
