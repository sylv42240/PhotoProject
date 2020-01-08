package fr.peaky.photographieproject.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.adapter.CustomScrollListener
import fr.peaky.photographieproject.ui.adapter.PelliculeAdapter
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import kotlinx.android.synthetic.main.activity_pellicule_list.*


class PelliculeListActivity : AppCompatActivity() {

    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val db = FirebaseFirestore.getInstance()
    private val adapter = PelliculeAdapter()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pellicule_list)
        val view: View = findViewById(android.R.id.content)
        getDatabaseInfos(view)
    }


    private fun getDatabaseInfos(view: View) {
        if (isOnline(this)) {
            db.collection(PELLICULE_VALUE)
                .whereEqualTo(USER_PARAMETER, userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val pelliculeList = mutableListOf<Pellicule>()
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
        Toast.makeText(this, "PAS DE PEL", Toast.LENGTH_SHORT).show()
    }

    private fun updateRecyclerView(pelliculeList: MutableList<Pellicule>) {
        adapter.updatePelliculeList(pelliculeList)
        pellicule_recycler_view.adapter = adapter
        pellicule_recycler_view.addOnScrollListener(CustomScrollListener(this))
        pellicule_recycler_view.layoutManager = LinearLayoutManager(this)
        pellicule_list_progress_bar.hide()
    }

    fun notifyMovingScroll(responseCode: Int){
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
}
