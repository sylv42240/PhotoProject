package fr.peaky.photographieproject.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.extension.convertTimeToDate
import fr.peaky.photographieproject.data.extension.getCircularProgressDrawable
import fr.peaky.photographieproject.data.model.Photo
import fr.peaky.photographieproject.ui.activity.PhotoDetailActivity
import fr.peaky.photographieproject.ui.activity.SequenceDetailActivity
import fr.peaky.photographieproject.ui.component.inflate
import kotlinx.android.synthetic.main.photo_item_holder.view.*


const val PHOTO_EXTRA_KEY = "photo_extra_key"
const val PHOTO_STATE_EXTRA_KEY = "photo_state_extra_key"
const val MODIFICATION_MODE = "modification"
const val CREATION_MODE = "creation"

class PhotoAdapter : RecyclerView.Adapter<PhotoViewHolder>() {

    lateinit var listener: (Photo) -> Unit
    private var photos = emptyList<Photo>()

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindPellicule(photos[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflatedView: View = parent.inflate(R.layout.photo_item_holder, false)
        return PhotoViewHolder(inflatedView, listener)
    }

    fun updatePhotoList(photos: List<Photo>) {
        this.photos = photos
        notifyDataSetChanged()
    }

}

class PhotoViewHolder(view: View, listener: (Photo) -> Unit) : RecyclerView.ViewHolder(view) {

    private val rootView = view
    private var photo: Photo? = null

    init {
        rootView.setOnClickListener {
            val intent = Intent(it.context, PhotoDetailActivity::class.java)
            intent.putExtra(PHOTO_EXTRA_KEY, photo)
            intent.putExtra(PHOTO_STATE_EXTRA_KEY, MODIFICATION_MODE)
            it.context.startActivity(intent)
        }
        rootView.setOnLongClickListener{
            photo?.let { it1 -> listener(it1) }
            return@setOnLongClickListener true
        }
    }

    fun bindPellicule(photo: Photo) {
        this.photo = photo
        rootView.photo_time_item.text = convertTimeToDate(photo.time)
        val circularProgressDrawable = getCircularProgressDrawable(rootView.context)
        circularProgressDrawable.start()
        Glide.with(rootView.context).load(photo.imagePath)
            .placeholder(circularProgressDrawable)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(rootView.photo_image_item)
    }
}

class CustomPhotoScrollListener(sequenceDetailActivity: SequenceDetailActivity) :
    RecyclerView.OnScrollListener() {

    private val sequenceDetailActivity = sequenceDetailActivity

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        when {
            dy > 0 -> sequenceDetailActivity.notifyPhotoListMovingScroll(1)
            dy < 0 -> sequenceDetailActivity.notifyPhotoListMovingScroll(2)
            else -> sequenceDetailActivity.notifyPhotoListMovingScroll(0)
        }
    }
}
