package fr.peaky.photographieproject.data.manager.profile

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.peaky.photographieproject.data.exception.NetworkException
import fr.peaky.photographieproject.data.exception.RealTimeDBException
import fr.peaky.photographieproject.data.extension.isOnline
import io.reactivex.Single


const val USERS_COLLECTION = "Users"
const val EMAIL_PARAMETER = "email"
const val USERNAME_PARAMETER = "username"

class ProfileManagerImpl(private val db: FirebaseDatabase) :
    ProfileManager {

    override fun initUserData(context: Context, user: FirebaseUser): Single<Boolean> {
        val userReference = db.getReference(USERS_COLLECTION).child(user.uid)
        return Single.create { subscriber ->
            if (isOnline(context)) {
                userReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.hasChild(EMAIL_PARAMETER)) {
                            userReference.child(EMAIL_PARAMETER).setValue(user.email)
                        }
                        if (!dataSnapshot.hasChild(USERNAME_PARAMETER)) {
                            userReference.child(USERNAME_PARAMETER)
                                .setValue(user.displayName.toString())
                        }
                        subscriber.onSuccess(true)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        subscriber.onError(RealTimeDBException())
                    }
                })
            } else {
                subscriber.onError(NetworkException())
            }
        }
    }
}