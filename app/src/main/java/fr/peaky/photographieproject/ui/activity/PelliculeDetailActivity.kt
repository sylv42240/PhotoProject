package fr.peaky.photographieproject.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.model.Pellicule
import fr.peaky.photographieproject.ui.adapter.PELLICULE_EXTRA_KEY

class PelliculeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pellicule_detail)
        val pellicule: Pellicule = intent.getSerializableExtra(PELLICULE_EXTRA_KEY) as Pellicule
        val pelliculeId: String = pellicule.id
        
    }
}
