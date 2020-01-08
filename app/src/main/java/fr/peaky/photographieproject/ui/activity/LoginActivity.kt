package fr.peaky.photographieproject.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import fr.peaky.photographieproject.*
import fr.peaky.photographieproject.ui.component.ErrorDisplayComponent
import fr.peaky.photographieproject.ui.component.ErrorTranslator
import fr.peaky.photographieproject.data.exception.UserNotFoundException
import fr.peaky.photographieproject.data.extension.isOnline
import fr.peaky.photographieproject.data.extension.observeSafe
import fr.peaky.photographieproject.data.manager.profile.ProfileManagerImpl
import fr.peaky.photographieproject.data.repository.ProfileRepository
import fr.peaky.photographieproject.ui.viewmodel.LoginViewModel

const val REQUEST_CODE = 4500


class LoginActivity : AppCompatActivity() {

    private var providers = emptyList<AuthUI.IdpConfig>()
    private val loginViewModel: LoginViewModel =
        LoginViewModel(
            ProfileRepository(
                ProfileManagerImpl(
                    FirebaseDatabase.getInstance()
                ), this
            )
        )
    private val errorDisplayComponent = ErrorDisplayComponent(ErrorTranslator(this))
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Handler().postDelayed({
            initConnection(findViewById(android.R.id.content))
        }, 1500)

    }


    private fun initConnection(view: View) {
        if (isOnline(this)) {
            if (auth.currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.fadein,
                    R.anim.fadeout
                )
                this.finish()
            } else {
                providers = listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.PhoneBuilder().build()
                )
                showSignInOptions(providers, view)
            }
        } else {
            showError(view)
        }
    }

    private fun showError(view: View) {
        val errorSnackBar: Snackbar = Snackbar.make(
            view,
            "Verifier votre connexion internet", Snackbar.LENGTH_INDEFINITE
        )
        errorSnackBar.setAction("Réessayer") { actionView ->
            initConnection(
                actionView
            )
        }
        errorSnackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                R.color.snack_bar_color
            )
        )
        errorSnackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            .setTextColor(ContextCompat.getColor(view.context,
                R.color.color_background
            ))
        errorSnackBar.view.findViewById<Button>(com.google.android.material.R.id.snackbar_action)
            .setTextColor(ContextCompat.getColor(view.context,
                R.color.color_background
            ))
        errorSnackBar.show()
    }

    private fun showSignInOptions(providers: List<AuthUI.IdpConfig>, view: View) {
        if (isOnline(this)) {
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.ic_launcher)
                    .build(), REQUEST_CODE
            )
        } else {
            showError(view)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val user = auth.currentUser
                searchIfUserExist(user, this, findViewById(android.R.id.content))
            } else {
                showSignInOptions(providers, findViewById(android.R.id.content))
            }
        } else {
            showSignInOptions(providers, findViewById(android.R.id.content))
        }
    }


    private fun searchIfUserExist(user: FirebaseUser?, activity: LoginActivity, view: View) {
        if (user != null) {
            loginViewModel.initUserInfo(user)
            loginViewModel.loginLiveData.observeSafe(this) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.fadein,
                    R.anim.fadeout
                )
                activity.finish()
            }
            loginViewModel.errorLiveData.observeSafe(this) {
                errorDisplayComponent.displayError(it, view)
            }
        } else {
            errorDisplayComponent.displayError(UserNotFoundException(), view)
        }

    }

}
