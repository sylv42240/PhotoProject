package fr.peaky.photographieproject.ui.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.*
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.extension.hide
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.extension.show
import fr.peaky.photographieproject.data.model.Appareil
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.data.model.Sequence
import fr.peaky.photographieproject.ui.adapter.CustomSequenceScrollListener
import fr.peaky.photographieproject.ui.adapter.PELLICULE_EXTRA_KEY
import fr.peaky.photographieproject.ui.adapter.SequenceAdapter
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_pellicule_detail.*
import kotlinx.android.synthetic.main.activity_pellicule_detail.myBottomAppBar

const val NO_APPAREIL = "Aucun appareil"

class PelliculeDetailActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private val adapter = SequenceAdapter()
    private lateinit var alertDialog: Dialog
    private val sequenceList = mutableListOf<Sequence>()
    private val appareilList = mutableListOf<Appareil>()
    private var noElement = false
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var pelliculeId: String
    private val appareilNameList = mutableListOf<String>()
    var pelliculeCreated = Pellicule()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pellicule_detail)
        val pellicule: Pellicule = intent.getSerializableExtra(PELLICULE_EXTRA_KEY) as Pellicule
        pelliculeCreated = pellicule
        pelliculeId = pellicule.id
        pellicule_detail_name.text = pellicule.name
        pellicule_detail_iso.text = "ISO : ${pellicule.iso}"
        alertDialog = Dialog(this)
        adapter.listener = this::deleteSequenceToFirestore
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view, pellicule)
        researchFabMenuBar2.setOnClickListener {
            getAppareilListFromDatabase()
        }
    }


    private fun getDatabaseInfos(view: View, pellicule: Pellicule) {
        sequenceList.clear()
        if (isOnline(this)) {
            db.collection(SEQUENCE_VALUE)
                .whereEqualTo(PELLICULE_PARAMETER, pellicule.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.map { document ->
                            val sequence =
                                document.toObject(Sequence::class.java).apply {
                                    this.id = document.id
                                }
                            sequenceList.add(sequence)
                        }
                        if (sequenceList.isEmpty()) {
                            updateBackground()
                        } else {
                            updateRecyclerView(sequenceList)
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
        grp_list_progress_bar.hide()
        empty_grp_layout.show()
        noElement = true
    }

    private fun updateRecyclerView(sequenceList: MutableList<Sequence>) {
        sequenceList.sortBy {
            it.name.capitalize()
        }
        adapter.updateSequenceList(sequenceList)
        grp_recycler_view.adapter = adapter
        grp_recycler_view.addOnScrollListener(CustomSequenceScrollListener(this))
        grp_recycler_view.layoutManager = LinearLayoutManager(this)
        empty_grp_layout.hide()
        grp_list_progress_bar.hide()
    }

    fun notifyPelliculeDetailMovingScroll(responseCode: Int) {
        when (responseCode) {
            1 -> {
                myBottomAppBar.performHide()
                researchFabMenuBar2.hide()
            }
            2 -> {
                myBottomAppBar.performShow()
                researchFabMenuBar2.show()
            }
        }
    }


    private fun getAppareilListFromDatabase() {
        appareilList.clear()
        appareilNameList.clear()
        appareilList.add(Appareil("0", "0", NO_APPAREIL))
        appareilNameList.add(NO_APPAREIL)
        if (isOnline(this)) {
            db.collection(APPAREIL_VALUE)
                .whereEqualTo(USER_PARAMETER, userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.map { document ->
                            val appareil =
                                document.toObject(Appareil::class.java).apply {
                                    this.id = document.id
                                }
                            appareilList.add(appareil)
                            appareilNameList.add(appareil.name)
                        }
                        showAddSequenceDialog()
                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }


    private fun showAddSequenceDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.create_sequence_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.btn_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.appareil_spinner)
        val spinnerList = appareilNameList
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_adapter_layout)
        val editTextAppareil = dialogView.findViewById<EditText>(R.id.appareil_name)
        spinner.adapter = arrayAdapter
        buttonValidate.setOnClickListener {
            if (verifDialog(
                    spinner.selectedItem.toString(),
                    editTextAppareil.text.toString(),
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
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun verifDialog(
        appareilSpinner: String,
        appareilEditText: String,
        appareilIndex: Int
    ): Boolean {


        if (appareilSpinner == NO_APPAREIL){
            if(appareilEditText.length !in 5..40 && appareilEditText.isNotBlank()){
                Toast.makeText(
                    this,
                    "Le nom de l'appareil doit être compris entre 5 et 40 charactères",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            if (appareilEditText.isBlank()){
                Toast.makeText(
                    this,
                    "Aucun appareil selectionné",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        if (appareilSpinner != NO_APPAREIL){
            addSequenceToFirestore(appareilList[appareilIndex].id, pelliculeCreated)
        }else{
            addAppareilToFirestore(appareilEditText, userId)
        }


        return true
    }

    private fun addSequenceToFirestore(appareilId:String, pellicule: Pellicule){



        val sequence = HashMap<String, Any>()
        val numbers = mutableListOf<Int>()
        val name = if (sequenceList.isEmpty()){
            "Pellicule 1"
        }else{
            sequenceList.map {
                numbers.add(it.name.takeLast(1).toInt())
            }
            "Pellicule " +(numbers.max()?.plus(1))
        }

        println(pellicule.poses.toString())

        sequence["time"] = System.currentTimeMillis().toString()
        sequence["poses"] = pellicule.poses
        sequence["name"] = name
        sequence["appareilId"] = appareilId
        sequence["pelliculeId"] = pelliculeId


        db.collection(SEQUENCE_VALUE)
            .add(sequence)
            .addOnSuccessListener {
                Toast.makeText(this, "Ajoutée avec succès", Toast.LENGTH_LONG).show()
                val sequenceAdded = Sequence(it.id,pelliculeId, name, appareilId, pellicule.poses, System.currentTimeMillis().toString())
                if (noElement) {
                    sequenceList.add(sequenceAdded)
                    sequenceList.sortBy { sequence1 ->
                        sequence1.name.capitalize()
                    }
                    adapter.updateSequenceList(sequenceList)
                    grp_recycler_view.adapter = adapter
                    grp_recycler_view.addOnScrollListener(
                        CustomSequenceScrollListener(
                            this
                        )
                    )
                    grp_recycler_view.layoutManager = LinearLayoutManager(this)
                    empty_grp_layout.hide()
                } else {
                    sequenceList.add(sequenceAdded)
                    sequenceList.sortBy { sequence1 ->
                        sequence1.name.capitalize()
                    }
                    adapter.updateSequenceList(sequenceList)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être enregistré", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun addAppareilToFirestore(name: String, userId: String?) {
        val appareil = HashMap<String, String>()
        appareil["name"] = name
        appareil["userId"] = userId!!


        db.collection(APPAREIL_VALUE)
            .add(appareil)
            .addOnSuccessListener {
                val appareilAdded = Appareil(it.id, userId, name)
                addSequenceToFirestore(appareilAdded.id, pelliculeCreated)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être enregistré", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun deleteSequenceToFirestore(sequence: Sequence) {
        db.collection(SEQUENCE_VALUE)
            .document(sequence.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Supprimée avec succès", Toast.LENGTH_LONG).show()
                sequenceList.remove(sequence)
                adapter.updateSequenceList(sequenceList)
                adapter.notifyDataSetChanged()
                if (sequenceList.isEmpty()) {
                    updateBackground()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être supprimé", Toast.LENGTH_LONG)
                    .show()
            }
    }


}
