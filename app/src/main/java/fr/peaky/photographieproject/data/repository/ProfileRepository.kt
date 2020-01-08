package fr.peaky.photographieproject.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import fr.peaky.photographieproject.data.manager.profile.ProfileManager
import io.reactivex.Single

class ProfileRepository(private val profileManager: ProfileManager, private val context: Context) {

    fun initUserData(user: FirebaseUser): Single<Boolean> {
        return Single.defer {
            profileManager.initUserData(context, user)
        }
    }

}