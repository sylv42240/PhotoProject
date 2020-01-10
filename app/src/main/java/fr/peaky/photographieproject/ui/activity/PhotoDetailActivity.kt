package fr.peaky.photographieproject.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.APPAREIL_VALUE
import fr.peaky.photographieproject.data.OBJECTIF_VALUE
import fr.peaky.photographieproject.data.USER_PARAMETER
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.model.Appareil
import fr.peaky.photographieproject.data.model.Objectif
import fr.peaky.photographieproject.data.model.Photo
import fr.peaky.photographieproject.ui.adapter.CREATION_MODE
import fr.peaky.photographieproject.ui.adapter.PHOTO_EXTRA_KEY
import fr.peaky.photographieproject.ui.adapter.PHOTO_STATE_EXTRA_KEY
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_photo_detail.*

class PhotoDetailActivity : AppCompatActivity() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)
        val photo = intent.getSerializableExtra(PHOTO_EXTRA_KEY) as Photo
        val photoState = intent.getStringExtra(PHOTO_STATE_EXTRA_KEY)
        val view: View = findViewById(android.R.id.content)
        if (photoState == CREATION_MODE) {

        } else {
            getObjectifFromDatabase(photo, view)
        }


    }

    private fun getObjectifFromDatabase(photo: Photo, view: View) {
        if (isOnline(this)) {
            db.collection(OBJECTIF_VALUE)
                .document(photo.objectifId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val objectif = task.result?.toObject(Objectif::class.java)
                        photo_exposition.text = photo.exposition
                        photo_mode.text = photo.mode
                        photo_ouverture.text = photo.ouverture
                        photo_numero.text = photo.numberPhoto.toString()
                        photo_description.setText(photo.description)
                        Glide.with(this).load(photo.imagePath)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(photo_image)
                        if (objectif !=null){
                            photo_objectif.text = objectif.name
                        }else{
                            photo_objectif.text = getString(R.string.no_objectif)
                        }

                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }
}
