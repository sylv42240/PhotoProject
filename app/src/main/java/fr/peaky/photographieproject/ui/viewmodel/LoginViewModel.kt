package fr.peaky.photographieproject.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import fr.peaky.photographieproject.data.repository.ProfileRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class LoginViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    val errorLiveData: MutableLiveData<Throwable> = MutableLiveData()
    val loginLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val disposable = CompositeDisposable()

    fun initUserInfo(user: FirebaseUser){
        disposable.add(
            profileRepository.initUserData(user).subscribeOn(Schedulers.io()).subscribeBy(
                onError = {errorLiveData.postValue(it)},
                onSuccess = {loginLiveData.postValue(it)}
            )
        )
    }
}