package fr.peaky.photographieproject.ui.component

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import fr.peaky.photographieproject.R

class ErrorDisplayComponent(private val errorTranslator: ErrorTranslator) {
    fun displayError(throwable: Throwable, view: View?){
        view?.let {
            val snackBar = Snackbar.make(it, errorTranslator.translate(throwable), Snackbar.LENGTH_LONG)
            snackBar.view.setBackgroundColor(ContextCompat.getColor(it.context, R.color.snack_bar_color))
            snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(ContextCompat.getColor(it.context, R.color.color_background))
            snackBar.show()
        }
    }
}