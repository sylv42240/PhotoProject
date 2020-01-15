package fr.peaky.photographieproject.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.DEFAULT_IMAGE_PATH
import fr.peaky.photographieproject.data.PHOTO_VALUE
import fr.peaky.photographieproject.data.SEQUENCE_PARAMETER
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.extension.hide
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.extension.show
import fr.peaky.photographieproject.data.model.Photo
import fr.peaky.photographieproject.data.model.Sequence
import fr.peaky.photographieproject.ui.adapter.*
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_sequence_detail.*


class SequenceDetailActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private val photoList = mutableListOf<Photo>()
    private val adapter = PhotoAdapter()
    private var noElement = false
    private var sequenceId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence_detail)
        val sequence = intent.getSerializableExtra(SEQUENCE_EXTRA_KEY) as Sequence
        sequence_detail_name.text = sequence.name
        sequence_detail_poses.text = sequence.poses.toString() + " poses"
        val view: View = findViewById(android.R.id.content)
        sequenceId = sequence.id
        adapter.listener = this::deletePhotoToFirestore
        getDatabaseInfos(view, sequence.id)
        researchFabMenuBar4.setOnClickListener {
            val intent = Intent(it.context, PhotoDetailActivity::class.java)
            if (photoList.isNotEmpty()){
                intent.putExtra(PHOTO_EXTRA_KEY,
                    Photo(sequenceId = sequence.id,
                        imagePath = DEFAULT_IMAGE_PATH,
                        time = System.currentTimeMillis().toString(),
                        numberPhoto = photoList.last().numberPhoto + 1,
                        objectifId = photoList.last().objectifId,
                        mode = photoList.last().mode,
                        exposition = photoList.last().exposition,
                        ouverture = photoList.last().ouverture,
                        poses = sequence.poses))
            }else{
                intent.putExtra(PHOTO_EXTRA_KEY,
                    Photo(sequenceId = sequence.id,
                        imagePath = DEFAULT_IMAGE_PATH,
                        time = System.currentTimeMillis().toString(),
                        numberPhoto = 1,
                        poses = sequence.poses
                ))
            }

            intent.putExtra(PHOTO_STATE_EXTRA_KEY, CREATION_MODE)
            it.context.startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view, sequenceId)
    }

    private fun getDatabaseInfos(view: View, sequenceId: String) {
        photoList.clear()
        if (isOnline(this)) {
            db.collection(PHOTO_VALUE)
                .whereEqualTo(SEQUENCE_PARAMETER, sequenceId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.map { document ->
                            val photo = document.toObject(Photo::class.java).apply {
                                this.id = document.id
                            }
                            photoList.add(photo)
                        }
                        if (photoList.isEmpty()) {
                            updateBackground()
                        } else {
                            updateRecyclerView(photoList)
                        }
                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }

    private fun updateBackground() {
        sequence_detail_progress_bar.hide()
        empty_photo_layout.show()
        noElement = true
    }

    private fun updateRecyclerView(photoList: MutableList<Photo>) {
        photoList.sortBy {
            it.time
        }
        adapter.updatePhotoList(photoList)
        photos_recycler_view.adapter = adapter
        photos_recycler_view.addOnScrollListener(CustomPhotoScrollListener(this))
        photos_recycler_view.layoutManager = GridLayoutManager(this, 2)
        empty_photo_layout.hide()
        sequence_detail_progress_bar.hide()
    }

    fun notifyPhotoListMovingScroll(responseCode: Int) {
        when (responseCode) {
            1 -> {
                myBottomAppBar4.performHide()
                researchFabMenuBar4.hide()
            }
            2 -> {
                myBottomAppBar4.performShow()
                researchFabMenuBar4.show()
            }
        }
    }

    private fun deletePhotoToFirestore(photo: Photo) {
        db.collection(PHOTO_VALUE)
            .document(photo.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Supprimée avec succès", Toast.LENGTH_LONG).show()
                photoList.remove(photo)
                adapter.updatePhotoList(photoList)
                adapter.notifyDataSetChanged()
                if (photoList.isEmpty()) {
                    updateBackground()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être supprimé", Toast.LENGTH_LONG)
                    .show()
            }
    }

}
