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
import fr.peaky.photographieproject.data.model.GroupeSequence
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.adapter.CustomGroupeSequenceScrollListener
import fr.peaky.photographieproject.ui.adapter.GroupSequenceAdapter
import fr.peaky.photographieproject.ui.adapter.PELLICULE_EXTRA_KEY
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_pellicule_detail.*
import kotlinx.android.synthetic.main.activity_pellicule_detail.myBottomAppBar

const val NO_APPAREIL = "Aucun appareil"

class PelliculeDetailActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupSequenceAdapter()
    private lateinit var alertDialog: Dialog
    private val groupeSequenceList = mutableListOf<GroupeSequence>()
    private val appareilList = mutableListOf<Appareil>()
    private var noElement = false
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var pelliculeId: String
    private val appareilNameList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pellicule_detail)
        val pellicule: Pellicule = intent.getSerializableExtra(PELLICULE_EXTRA_KEY) as Pellicule
        pelliculeId = pellicule.id
        pellicule_detail_name.text = pellicule.name
        pellicule_detail_iso.text = pellicule.iso
        alertDialog = Dialog(this)
        adapter.listener = this::deleteGroupeSequenceToFirestore
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view, pellicule)
        researchFabMenuBar2.setOnClickListener {
            getAppareilListFromDatabase()
        }
    }


    private fun getDatabaseInfos(view: View, pellicule: Pellicule) {
        groupeSequenceList.clear()
        if (isOnline(this)) {
            db.collection(GROUPE_SEQUENCE_VALUE)
                .whereEqualTo(PELLICULE_PARAMETER, pellicule.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.map { document ->
                            val groupeSequence =
                                document.toObject(GroupeSequence::class.java).apply {
                                    this.id = document.id
                                }
                            groupeSequenceList.add(groupeSequence)
                        }
                        if (groupeSequenceList.isEmpty()) {
                            updateBackground()
                        } else {
                            updateRecyclerView(groupeSequenceList)
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

    private fun updateRecyclerView(groupeSequenceList: MutableList<GroupeSequence>) {
        groupeSequenceList.sortBy {
            it.name.capitalize()
        }
        adapter.updateGroupeSequenceList(groupeSequenceList)
        grp_recycler_view.adapter = adapter
        grp_recycler_view.addOnScrollListener(CustomGroupeSequenceScrollListener(this))
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
                        showAddGroupeSequenceDialog()
                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }


    private fun showAddGroupeSequenceDialog() {
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.create_grp_sequence_dialog, viewGroup, false)
        val buttonValidate = dialogView.findViewById<Button>(R.id.btn_validate)
        val buttonCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val spinner = dialogView.findViewById<Spinner>(R.id.appareil_spinner)
        val spinnerList = appareilNameList
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        val editText = dialogView.findViewById<EditText>(R.id.grp_sequence_name)
        val editTextAppareil = dialogView.findViewById<EditText>(R.id.appareil_name)
        spinner.adapter = arrayAdapter
        buttonValidate.setOnClickListener {
            if (verifDialog(
                    editText.text.toString(),
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
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun verifDialog(
        name: String,
        appareilSpinner: String,
        appareilEditText: String,
        appareilIndex: Int
    ): Boolean {
        if (name.length !in 5..40) {
            Toast.makeText(
                this,
                "Le nom du groupe doit être compris entre 5 et 40 charactères",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

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
            addGroupeSequenceToFirestore(name, appareilList[appareilIndex].id, pelliculeId)
        }else{
            addAppareilToFirestore(appareilEditText, userId, name)
        }


        return true
    }

    private fun addGroupeSequenceToFirestore(name: String, appareilId:String, pelliculeId:String){
        val groupeSequence = HashMap<String, String>()
        groupeSequence["name"] = name
        groupeSequence["appareilId"] = appareilId
        groupeSequence["pelliculeId"] = pelliculeId


        db.collection(GROUPE_SEQUENCE_VALUE)
            .add(groupeSequence)
            .addOnSuccessListener {
                Toast.makeText(this, "Ajouté avec succès", Toast.LENGTH_LONG).show()
                val groupeSequenceAdded = GroupeSequence(it.id, pelliculeId, appareilId, name)
                if (noElement) {
                    groupeSequenceList.add(groupeSequenceAdded)
                    groupeSequenceList.sortBy { groupeSequence1 ->
                        groupeSequence1.name.capitalize()
                    }
                    adapter.updateGroupeSequenceList(groupeSequenceList)
                    grp_recycler_view.adapter = adapter
                    grp_recycler_view.addOnScrollListener(
                        CustomGroupeSequenceScrollListener(
                            this
                        )
                    )
                    grp_recycler_view.layoutManager = LinearLayoutManager(this)
                    empty_grp_layout.hide()
                } else {
                    groupeSequenceList.add(groupeSequenceAdded)
                    groupeSequenceList.sortBy { groupeSequence1 ->
                        groupeSequence1.name.capitalize()
                    }
                    adapter.updateGroupeSequenceList(groupeSequenceList)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être enregistré", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun addAppareilToFirestore(name: String, userId: String?, groupeSequenceName: String) {
        val appareil = HashMap<String, String>()
        appareil["name"] = name
        appareil["userId"] = userId!!


        db.collection(APPAREIL_VALUE)
            .add(appareil)
            .addOnSuccessListener {
                val appareilAdded = Appareil(it.id, userId, name)
                addGroupeSequenceToFirestore(groupeSequenceName, appareilAdded.id, pelliculeId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être enregistré", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun deleteGroupeSequenceToFirestore(groupeSequence: GroupeSequence) {

        db.collection(GROUPE_SEQUENCE_VALUE)
            .document(groupeSequence.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Supprimé avec succès", Toast.LENGTH_LONG).show()
                groupeSequenceList.remove(groupeSequence)
                adapter.updateGroupeSequenceList(groupeSequenceList)
                adapter.notifyDataSetChanged()
                if (groupeSequenceList.isEmpty()) {
                    updateBackground()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Le document n'a pas pu être supprimé", Toast.LENGTH_LONG)
                    .show()
            }
    }


}
