package fr.peaky.photographieproject.ui.activity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.APPAREIL_VALUE
import fr.peaky.photographieproject.data.OBJECTIF_VALUE
import fr.peaky.photographieproject.data.PHOTO_VALUE
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
import kotlinx.android.synthetic.main.activity_pellicule_detail.*
import kotlinx.android.synthetic.main.activity_photo_detail.*

const val NO_OBJECTIF = "Aucun objectif"

class PhotoDetailActivity : AppCompatActivity() {

    private var photo: Photo = Photo()
    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private lateinit var alertDialog: Dialog
    private var objectifId = ""
    private var photoImagePath = ""
    private val objectifList = mutableListOf<Objectif>()
    private val objectifNameList = mutableListOf<String>()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)
        val photoIntent = intent.getSerializableExtra(PHOTO_EXTRA_KEY) as Photo
        this.photo = photoIntent
        photoImagePath = photo.imagePath
        alertDialog = Dialog(this)
        val photoState = intent.getStringExtra(PHOTO_STATE_EXTRA_KEY)
        val view: View = findViewById(android.R.id.content)
        if (photoState == CREATION_MODE) {
            photo_exposition.text = "1/125"
            photo_mode.text = "Portrait"
            photo_ouverture.text = "F5.6"
            photo_numero.text = "1"
            Glide.with(this).load(photo.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photo_image)
        } else {
            getObjectifFromDatabase(photo, view)
            objectifId = photo.objectifId
        }

        exposition_layout.setOnClickListener {
            showUpdateExpositionDialog()
        }

        mode_layout.setOnClickListener {
            showUpdateModeDialog()
        }

        ouverture_layout.setOnClickListener {
            showUpdateOuvertureDialog()
        }

        numero_layout.setOnClickListener {
            showUpdateNumberDialog()
        }

        objectif_layout.setOnClickListener {
            getObjectifListFromDatabase()
        }
    }


    private fun getObjectifListFromDatabase() {
        objectifList.clear()
        objectifNameList.clear()
        objectifList.add(Objectif("", "0", NO_APPAREIL))
        objectifNameList.add(NO_OBJECTIF)
        if (isOnline(this)) {
            db.collection(OBJECTIF_VALUE)
                .whereEqualTo(USER_PARAMETER, userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.map { document ->
                            val objectif =
                                document.toObject(Objectif::class.java).apply {
                                    this.id = document.id
                                }
                            objectifList.add(objectif)
                            objectifNameList.add(objectif.name)
                        }
                        showUpdateObjectifDialog()
                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }


    private fun showUpdateObjectifDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.add_photo_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.add_photo_dialog_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.add_photo_dialog_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.add_photo_dialog_spinner)
        val spinnerList = objectifNameList
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        val editTextObjectif = dialogView.findViewById<EditText>(R.id.add_photo_dialog_edit)
        spinner.adapter = arrayAdapter
        if (photo_objectif.text.toString()!= NO_OBJECTIF){
            spinner.setSelection(arrayAdapter.getPosition(photo_objectif.text.toString()))
        }

        buttonValidate.setOnClickListener {
            if (verifDialog(
                    spinner.selectedItem.toString(),
                    editTextObjectif.text.toString(),
                    spinner.selectedItemPosition
                )
            ) {
                alertDialog.dismiss()
            }
        }
        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    private fun verifDialog(
        objectifSpinner: String,
        objectifEditText: String,
        objectifIndex: Int
    ): Boolean {

        if (objectifSpinner == NO_OBJECTIF) {
            if (objectifEditText.length !in 5..40 && objectifEditText.isNotBlank()) {
                Toast.makeText(
                    this,
                    "Le nom de l'objectif doit être compris entre 5 et 40 charactères",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            if (objectifEditText.isBlank()) {
                Toast.makeText(
                    this,
                    "Aucun objectif selectionné",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        if (objectifSpinner != NO_OBJECTIF) {
            objectifId = objectifList[objectifIndex].id
            photo_objectif.text = objectifList[objectifIndex].name
        } else {
            addObjectifToFirestore(objectifEditText, userId)
        }


        return true
    }

    private fun addObjectifToFirestore(name: String, userId: String?) {
        val objectif = HashMap<String, String>()
        objectif["name"] = name
        objectif["userId"] = userId!!


        db.collection(OBJECTIF_VALUE)
            .add(objectif)
            .addOnSuccessListener {
                val objectifAdded = Objectif(it.id, userId, name)
                photo_objectif.text = objectifAdded.name
                objectifId = objectifAdded.id
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être enregistré", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun getObjectifFromDatabase(photo: Photo, view: View) {

        if (photo.objectifId != "") {
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
                            researchFabMenuBar5.setOnClickListener {
                                updatePhotoFromDatabase(photo, view)
                            }
                            if (objectif != null) {
                                photo_objectif.text = objectif.name
                            } else {
                                photo_objectif.text = getString(R.string.no_objectif)
                            }

                        } else {
                            errorDisplayComponent.displayError(FirestoreException(), view)
                        }
                    }
            } else {
                errorDisplayComponent.displayError(NetworkException(), view)
            }
        } else {
            photo_exposition.text = photo.exposition
            photo_mode.text = photo.mode
            photo_ouverture.text = photo.ouverture
            photo_numero.text = photo.numberPhoto.toString()
            photo_description.setText(photo.description)
            Glide.with(this).load(photo.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photo_image)
            researchFabMenuBar5.setOnClickListener {
                updatePhotoFromDatabase(photo, view)
            }
            photo_objectif.text = getString(R.string.no_objectif)
        }

    }


    private fun updatePhotoFromDatabase(photo: Photo, view: View) {

        val photoUpdated = HashMap<String, Any>()
        photoUpdated["description"] = photo_description.text.toString()
        photoUpdated["exposition"] = photo_exposition.text.toString()
        photoUpdated["imagePath"] = photoImagePath
        photoUpdated["mode"] = photo_mode.text.toString()
        photoUpdated["numberPhoto"] = photo_numero.text.toString().toInt()
        photoUpdated["objectifId"] = objectifId
        photoUpdated["ouverture"] = photo_ouverture.text.toString()

        if (isOnline(this)) {
            db.collection(PHOTO_VALUE)
                .document(photo.id)
                .update(photoUpdated)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Modifiée avec succès", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }

    private fun showUpdateExpositionDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.update_photo_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.update_photo_dialog_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.update_photo_dialog_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.update_photo_dialog_spinner)
        val title = dialogView.findViewById<TextView>(R.id.update_photo_dialog_title)
        title.text = "Modifier l'Exposition"
        val attribute = dialogView.findViewById<TextView>(R.id.update_photo_dialog_attribute)
        attribute.text = "Exposition"
        val spinnerList = arrayOf(
            "1/1000",
            "1/500",
            "1/250",
            "1/125",
            "1/60",
            "1/30",
            "1/15",
            "1/8",
            "1/4",
            "1/2"
        )
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        spinner.adapter = arrayAdapter
        spinner.setSelection(arrayAdapter.getPosition(photo_exposition.text.toString()))
        buttonValidate.setOnClickListener {
            photo_exposition.text = spinner.selectedItem.toString()
            alertDialog.dismiss()
        }
        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    private fun showUpdateModeDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.update_photo_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.update_photo_dialog_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.update_photo_dialog_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.update_photo_dialog_spinner)
        val title = dialogView.findViewById<TextView>(R.id.update_photo_dialog_title)
        title.text = "Modifier le mode"
        val attribute = dialogView.findViewById<TextView>(R.id.update_photo_dialog_attribute)
        attribute.text = "Mode"
        val spinnerList = arrayOf(
            "Automatique",
            "Scène",
            "Portrait",
            "Paysage",
            "Sport",
            "Priorité ouverture",
            "Priorité vitesse",
            "Manuel"
        )
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        spinner.adapter = arrayAdapter
        spinner.setSelection(arrayAdapter.getPosition(photo_mode.text.toString()))
        buttonValidate.setOnClickListener {
            photo_mode.text = spinner.selectedItem.toString()
            alertDialog.dismiss()
        }
        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun showUpdateOuvertureDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.update_photo_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.update_photo_dialog_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.update_photo_dialog_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.update_photo_dialog_spinner)
        val title = dialogView.findViewById<TextView>(R.id.update_photo_dialog_title)
        title.text = "Modifier l'Ouverture"
        val attribute = dialogView.findViewById<TextView>(R.id.update_photo_dialog_attribute)
        attribute.text = "Ouverture"
        val spinnerList = arrayOf(
            "F32",
            "F22",
            "F16",
            "F11",
            "F8",
            "F5.6",
            "F4",
            "F2.8",
            "F2",
            "F1.4"
        )
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        spinner.adapter = arrayAdapter
        spinner.setSelection(arrayAdapter.getPosition(photo_ouverture.text.toString()))
        buttonValidate.setOnClickListener {
            photo_ouverture.text = spinner.selectedItem.toString()
            alertDialog.dismiss()
        }
        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    private fun showUpdateNumberDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.edit_photo_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.edit_photo_dialog_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.edit_photo_dialog_cancel)
        val editText = dialogView.findViewById<EditText>(R.id.edit_photo_dialog_edit)
        editText.setText(photo_numero.text.toString())
        buttonValidate.setOnClickListener {
            photo_numero.text = editText.text.toString()
            alertDialog.dismiss()
        }
        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}
