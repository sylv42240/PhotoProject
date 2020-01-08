package fr.peaky.photographieproject.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import fr.peaky.photographieproject.data.exception.FirestoreException
import fr.peaky.photographieproject.data.model.Pellicule


class PelliculeListActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pellicule_list)
        val view: View = findViewById(android.R.id.content)
        getDatabaseInformations(view)
    }


    private fun getDatabaseInformations(view: View) {
        db.collection("users")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    val pelliculeList = mutableListOf<Pellicule>()
                    task.result?.map { document ->
                        val pellicule = document.toObject(Pellicule::class.java)
                        pelliculeList.add(pellicule)
                    }
                    if (pelliculeList.isEmpty()) {
                        //TODO: Mettre un background : pas de pellicule
                    } else {
                        //TODO: RecyclerView a add
                    }
                } else {
                    errorDisplayComponent.displayError(FirestoreException(), view)
                }
            })
    }
}
