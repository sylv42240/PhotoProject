package fr.peaky.photographieproject.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.peaky.photographieproject.R.*
import fr.peaky.photographieproject.data.extension.hide
import fr.peaky.photographieproject.data.extension.show
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.activity.PelliculeDetailActivity
import fr.peaky.photographieproject.ui.activity.PelliculeListActivity
import fr.peaky.photographieproject.ui.component.inflate
import kotlinx.android.synthetic.main.pellicule_item_holder.view.*
import android.animation.ObjectAnimator
import android.view.animation.OvershootInterpolator

class PelliculeAdapter : RecyclerView.Adapter<PelliculeViewHolder>() {

    var pellicules = emptyList<Pellicule>()
    private var startOffset = 0

    override fun getItemCount(): Int {
        return pellicules.size
    }

    override fun onBindViewHolder(holder: PelliculeViewHolder, position: Int) {
        holder.bindPellicule(pellicules[position])

    }

    private fun animateRecyclerView(view: View) {


        val fadeAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        fadeAnimator.duration = 900
        fadeAnimator.startDelay = startOffset.toLong()
        fadeAnimator.start()

        val translateAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f)
        translateAnimator.duration = 800
        translateAnimator.startDelay = startOffset.toLong()
        translateAnimator.interpolator = OvershootInterpolator()
        translateAnimator.start()

        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 2f, 0.8f, 1.0f)
        scaleXAnimator.duration = 800
        scaleXAnimator.startDelay = startOffset.toLong()
        scaleXAnimator.start()

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 2f, 0.8f, 1.0f)
        scaleYAnimator.duration = 700
        scaleYAnimator.startDelay = startOffset.toLong()
        scaleYAnimator.start()


    }


    override fun onViewAttachedToWindow(holder: PelliculeViewHolder) {
        animateRecyclerView(holder.itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelliculeViewHolder {
        val inflatedView: View = parent.inflate(layout.pellicule_item_holder, false)
//        animateRecyclerView(inflatedView)

        return PelliculeViewHolder(inflatedView)
    }

    fun updatePelliculeList(pellicules: List<Pellicule>) {
        this.pellicules = pellicules
        notifyDataSetChanged()
    }

}

class PelliculeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val rootView = view
    private var pellicule: Pellicule? = null

    init {
        rootView.setOnClickListener {
            rootView.clickOnPelliculeInfo.show()
        }
        rootView.clickOnPelliculeInfo.modify.setOnClickListener{
            navigateToPelliculeDetailActivity(it)
        }

    }

    fun bindPellicule(pellicule: Pellicule) {
        this.pellicule = pellicule
        rootView.clickOnPelliculeInfo.hide()
        rootView.pellicule_name.text = pellicule.name
        rootView.iso_label.text = pellicule.iso
    }

    private fun navigateToPelliculeDetailActivity(it: View){
        val intent = Intent(it.context, PelliculeDetailActivity::class.java)
        intent.putExtra(PELLICULE_EXTRA_KEY, pellicule)
        it.context.startActivity(intent)
    }

    companion object {
        const val PELLICULE_EXTRA_KEY = "pellicule_extra_key"
    }
}

class CustomScrollListener(pelliculeListActivity: PelliculeListActivity) :
    RecyclerView.OnScrollListener() {

    private val pelliculeListActivity = pelliculeListActivity

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        when {
            dy > 0 -> pelliculeListActivity.notifyMovingScroll(1)
            dy < 0 -> pelliculeListActivity.notifyMovingScroll(2)
            else -> pelliculeListActivity.notifyMovingScroll(0)
        }
    }
}

