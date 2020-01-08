package fr.peaky.photographieproject.data.manager.profile

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Single

interface ProfileManager {
    fun initUserData(context: Context, user: FirebaseUser): Single<Boolean>
}