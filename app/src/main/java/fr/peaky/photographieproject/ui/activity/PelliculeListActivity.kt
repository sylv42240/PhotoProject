package fr.peaky.photographieproject.ui.activity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R.id
import fr.peaky.photographieproject.R.layout
import fr.peaky.photographieproject.data.PELLICULE_VALUE
import fr.peaky.photographieproject.data.USER_PARAMETER
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.extension.hide
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.extension.show
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.adapter.CustomScrollListener
import fr.peaky.photographieproject.ui.adapter.PelliculeAdapter
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import io.alterac.blurkit.BlurKit
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
        setContentView(layout.activity_pellicule_list)
        alertDialog = Dialog(this)
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view)
        researchFabMenuBar.setOnClickListener {
            showAddPelliculeDialog()
        }
        BlurKit.init(this)
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
                            val pellicule = document.toObject(Pellicule::class.java)
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
        adapter.updatePelliculeList(pelliculeList)
        pellicule_recycler_view.adapter = adapter
        pellicule_recycler_view.addOnScrollListener(CustomScrollListener(this))
        pellicule_recycler_view.layoutManager = LinearLayoutManager(this)
        empty_pellicule_layout.hide()
        pellicule_list_progress_bar.hide()
    }

    fun notifyMovingScroll(responseCode: Int) {
        when (responseCode) {
            1 -> {
                myBottomAppBar.performHide()
                researchFabMenuBar.hide()
                blurLayout.visibility = GONE
            }
            2 -> {
                myBottomAppBar.performShow()
                researchFabMenuBar.show()
                blurLayout.visibility = VISIBLE
            }
        }
    }


    private fun showAddPelliculeDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(layout.create_pellicule_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(id.btn_validate)
        val buttonCancel = dialogView.findViewById<Button>(id.btn_cancel)
        val spinner = dialogView.findViewById<Spinner>(id.pellicule_iso_spinner)
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
        val editText = dialogView.findViewById<EditText>(id.pellicule_name)
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
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
                    adapter.updatePelliculeList(pelliculeList)
                    pellicule_recycler_view.adapter = adapter
                    pellicule_recycler_view.addOnScrollListener(CustomScrollListener(this))
                    pellicule_recycler_view.layoutManager = LinearLayoutManager(this)
                    empty_pellicule_layout.hide()
                } else {
                    pelliculeList.add(pelliculeAdded)
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

}
