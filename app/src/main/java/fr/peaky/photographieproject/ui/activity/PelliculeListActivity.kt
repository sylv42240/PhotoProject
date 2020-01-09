package fr.peaky.photographieproject.ui.activity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.PELLICULE_VALUE
import fr.peaky.photographieproject.data.USER_PARAMETER
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.extension.hide
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.extension.show
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.adapter.CustomPelliculeScrollListener
import fr.peaky.photographieproject.ui.adapter.PelliculeAdapter
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_pellicule_list.*


class PelliculeListActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private val adapter = PelliculeAdapter()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var alertDialog: Dialog
    private val pelliculeList = mutableListOf<Pellicule>()
    private var noElement = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pellicule_list)
        alertDialog = Dialog(this)
        adapter.listener = this::deletePelliculeToFirestore
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view)
        researchFabMenuBar.setOnClickListener {
            showAddPelliculeDialog()
        }
    }


    private fun getDatabaseInfos(view: View) {
        pelliculeList.clear()
        if (isOnline(this)) {
            db.collection(PELLICULE_VALUE)
                .whereEqualTo(USER_PARAMETER, userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.map { document ->
                            val pellicule = document.toObject(Pellicule::class.java).apply {
                                this.id = document.id
                            }
                            pelliculeList.add(pellicule)
                        }
                        if (pelliculeList.isEmpty()) {
                            updateBackground()
                        } else {
                            updateRecyclerView(pelliculeList)
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
        pellicule_list_progress_bar.hide()
        empty_pellicule_layout.show()
        noElement = true
    }

    private fun updateRecyclerView(pelliculeList: MutableList<Pellicule>) {
        pelliculeList.sortBy {
            it.name.capitalize()
        }
        adapter.updatePelliculeList(pelliculeList)
        pellicule_recycler_view.adapter = adapter
        pellicule_recycler_view.addOnScrollListener(CustomPelliculeScrollListener(this))
        pellicule_recycler_view.layoutManager = LinearLayoutManager(this)
        empty_pellicule_layout.hide()
        pellicule_list_progress_bar.hide()
    }

    fun notifyPelliculeListMovingScroll(responseCode: Int) {
        when (responseCode) {
            1 -> {
                myBottomAppBar.performHide()
                researchFabMenuBar.hide()
            }
            2 -> {
                myBottomAppBar.performShow()
                researchFabMenuBar.show()
            }
        }
    }


    private fun showAddPelliculeDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.create_pellicule_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.btn_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.pellicule_iso_spinner)
        val spinnerList = arrayOf(
            "ISO 50",
            "ISO 100",
            "ISO 200",
            "ISO 400",
            "ISO 800",
            "ISO 1 600",
            "ISO 12 800",
            "ISO 25 600"
        )
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        val editText = dialogView.findViewById<EditText>(R.id.pellicule_name)
        spinner.adapter = arrayAdapter
        buttonValidate.setOnClickListener {
            if (addPelliculeToFirestore(
                    editText.text.toString(),
                    spinner.selectedItem.toString()
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

    private fun addPelliculeToFirestore(name: String, iso: String): Boolean {
        if (name.length !in 5..40) {
            Toast.makeText(
                this,
                "Le nom de la pellicule doit être compris entre 5 et 40 charactères",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val pellicule = HashMap<String, String>()
        pellicule["name"] = name
        pellicule["iso"] = iso
        pellicule["userId"] = userId.toString()


        db.collection(PELLICULE_VALUE)
            .add(pellicule)
            .addOnSuccessListener {
                Toast.makeText(this, "Ajouté avec succès", Toast.LENGTH_LONG).show()
                val pelliculeAdded = Pellicule(it.id, userId.toString(), name, iso)
                if (noElement) {
                    pelliculeList.add(pelliculeAdded)
                    pelliculeList.sortBy {pellicule ->
                        pellicule.name.capitalize()
                    }
                    adapter.updatePelliculeList(pelliculeList)
                    pellicule_recycler_view.adapter = adapter
                    pellicule_recycler_view.addOnScrollListener(CustomPelliculeScrollListener(this))
                    pellicule_recycler_view.layoutManager = LinearLayoutManager(this)
                    empty_pellicule_layout.hide()
                } else {
                    pelliculeList.add(pelliculeAdded)
                    pelliculeList.sortBy {pellicule ->
                        pellicule.name.capitalize()
                    }
                    adapter.updatePelliculeList(pelliculeList)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être enregistré", Toast.LENGTH_LONG)
                    .show()
            }
        return true
    }

    private fun deletePelliculeToFirestore(pellicule: Pellicule) {

        db.collection(PELLICULE_VALUE)
            .document(pellicule.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Supprimé avec succès", Toast.LENGTH_LONG).show()
                pelliculeList.remove(pellicule)
                adapter.updatePelliculeList(pelliculeList)
                adapter.notifyDataSetChanged()
                if (pelliculeList.isEmpty()){
                    updateBackground()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être supprimé", Toast.LENGTH_LONG).show()
            }
    }

}
