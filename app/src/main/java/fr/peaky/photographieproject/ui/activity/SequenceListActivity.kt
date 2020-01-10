package fr.peaky.photographieproject.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.APPAREIL_VALUE
import fr.peaky.photographieproject.data.GRP_SEQUENCE_PARAMETER
import fr.peaky.photographieproject.data.SEQUENCE_VALUE
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.extension.hide
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.extension.show
import fr.peaky.photographieproject.data.model.Appareil
import fr.peaky.photographieproject.data.model.GroupeSequence
import fr.peaky.photographieproject.data.model.Sequence
import fr.peaky.photographieproject.ui.adapter.CustomSequenceScrollListener
import fr.peaky.photographieproject.ui.adapter.GROUPE_SEQUENCE_EXTRA_KEY
import fr.peaky.photographieproject.ui.adapter.SequenceAdapter
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_pellicule_detail.*
import kotlinx.android.synthetic.main.activity_sequence_list.*

class SequenceListActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private val sequenceList = mutableListOf<Sequence>()
    private val adapter = SequenceAdapter()
    private var noElement = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence_list)
        val groupeSequence =
            intent.getSerializableExtra(GROUPE_SEQUENCE_EXTRA_KEY) as GroupeSequence
        adapter.listener = this::deleteSequenceToFirestore
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view, groupeSequence.id)
        retrieveAppareilNameFromDatabase(view, groupeSequence.appareilId, groupeSequence.name)
        researchFabMenuBar3.setOnClickListener {
            addSequenceToDatabase(groupeSequence.id)
        }
    }

    private fun addSequenceToDatabase(groupeSequenceId: String) {

        val sequence = HashMap<String, String>()
        val numbers = mutableListOf<Int>()
        val name = if (sequenceList.isEmpty()){
            "Séquence 1"
        }else{
            sequenceList.map {
                numbers.add(it.name.takeLast(1).toInt())
            }
            "Séquence " +(numbers.max()?.plus(1))
        }
        sequence["name"] = name
        sequence["groupeSequenceId"] = groupeSequenceId

        db.collection(SEQUENCE_VALUE)
            .add(sequence)
            .addOnSuccessListener {
                Toast.makeText(this, "Ajoutée avec succès", Toast.LENGTH_LONG).show()
                val sequenceAdded = Sequence(it.id, groupeSequenceId, name)
                if (noElement) {
                    sequenceList.add(sequenceAdded)
                    sequenceList.sortBy { sequence ->
                        sequence.name.capitalize()
                    }
                    adapter.updateGroupeSequenceList(sequenceList)
                    sequence_recycler_view.adapter = adapter
                    sequence_recycler_view.addOnScrollListener(CustomSequenceScrollListener(this))
                    sequence_recycler_view.layoutManager = LinearLayoutManager(this)
                    empty_sequence_layout.hide()
                } else {
                    sequenceList.add(sequenceAdded)
                    sequenceList.sortBy { sequence ->
                        sequence.name.capitalize()
                    }
                    adapter.updateGroupeSequenceList(sequenceList)
                    adapter.notifyDataSetChanged()
                }
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
                adapter.updateGroupeSequenceList(sequenceList)
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

    private fun retrieveAppareilNameFromDatabase(view: View, appareilId: String, name: String) {
        if (isOnline(this)) {
            db.collection(APPAREIL_VALUE)
                .document(appareilId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val appareil = task.result?.toObject(Appareil::class.java)
                        grp_detail_name.text = name
                        grp_detail_appareil.text = appareil?.name
                    } else {
                        errorDisplayComponent.displayError(FirestoreException(), view)
                    }
                }
        } else {
            errorDisplayComponent.displayError(NetworkException(), view)
        }
    }

    private fun getDatabaseInfos(view: View, groupeSequenceId: String) {
        sequenceList.clear()
        if (isOnline(this)) {
            db.collection(SEQUENCE_VALUE)
                .whereEqualTo(GRP_SEQUENCE_PARAMETER, groupeSequenceId)
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
        sequence_list_progress_bar.hide()
        empty_sequence_layout.show()
        noElement = true
    }

    private fun updateRecyclerView(sequenceList: MutableList<Sequence>) {
        sequenceList.sortBy {
            it.name.capitalize()
        }
        adapter.updateGroupeSequenceList(sequenceList)
        sequence_recycler_view.adapter = adapter
        sequence_recycler_view.addOnScrollListener(CustomSequenceScrollListener(this))
        sequence_recycler_view.layoutManager = LinearLayoutManager(this)
        empty_sequence_layout.hide()
        sequence_list_progress_bar.hide()
    }

    fun notifySequenceListMovingScroll(responseCode: Int) {
        when (responseCode) {
            1 -> {
                myBottomAppBar.performHide()
                researchFabMenuBar3.hide()
            }
            2 -> {
                myBottomAppBar.performShow()
                researchFabMenuBar3.show()
            }
        }
    }
}
