package fr.peaky.photographieproject.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.component.inflate
import kotlinx.android.synthetic.main.pellicule_item_holder.view.*

class PelliculeAdapter : RecyclerView.Adapter<PelliculeViewHolder>() {

    var pellicules = emptyList<Pellicule>()

    override fun getItemCount(): Int {
        return pellicules.size
    }

    override fun onBindViewHolder(holder: PelliculeViewHolder, position: Int) {
        holder.bindPellicule(pellicules[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelliculeViewHolder {
        val inflatedView: View = parent.inflate(R.layout.pellicule_item_holder, false)
        return PelliculeViewHolder(inflatedView)
    }

    fun updatePelliculeList(pellicules: List<Pellicule>) {
        this.pellicules = pellicules
        notifyDataSetChanged()
    }

}

class PelliculeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val rootview = view
    private var pellicule: Pellicule? = null

    init {
        rootview.setOnClickListener {
            val intent = Intent(PelliculeListActivity, PelliculeDetailActivity.class. java)
            intent.putExtra(PELLICULE_EXTRA_KEY, pellicule)
            startActivity(intent)
        }
    }

    fun bindPellicule(pellicule: Pellicule) {
        this.pellicule = pellicule
        rootview.pelliculeName.text = pellicule.name
        rootview.isoLabel.text = pellicule.iso
        rootview.countNumberGroupeSequence.text = pellicule.groupeSequenceCount
    }

    companion object {
        const val PELLICULE_EXTRA_KEY = "pellicule_extra_key"
    }
}
