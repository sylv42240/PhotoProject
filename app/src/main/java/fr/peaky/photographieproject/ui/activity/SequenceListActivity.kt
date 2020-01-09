package fr.peaky.photographieproject.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.model.GroupeSequence
import fr.peaky.photographieproject.ui.adapter.GROUPE_SEQUENCE_EXTRA_KEY

class SequenceListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence_list)
        val groupeSequence = intent.getSerializableExtra(GROUPE_SEQUENCE_EXTRA_KEY) as GroupeSequence
    }
}
