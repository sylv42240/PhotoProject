package fr.peaky.photographieproject.ui.component

import android.content.Context
import fr.peaky.photographieproject.R
import fr.peaky.photographieproject.data.exception.*

class ErrorTranslator(private val context: Context) {
    fun translate(throwable: Throwable): String{
        return context.getString(
            when(throwable){
                is NetworkException -> R.string.exception_network
                is FirestoreException -> R.string.exception_firestore
                is RealTimeDBException -> R.string.exception_firestore
                is UserNotFoundException -> R.string.exception_user
                else -> R.string.exception_default
            }
        )
    }
}