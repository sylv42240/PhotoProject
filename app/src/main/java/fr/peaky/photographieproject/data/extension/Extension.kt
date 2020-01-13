package fr.peaky.photographieproject.data.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
fun isOnline(context: Context): Boolean {
    var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    result = 2
                } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    result = 1
                }
            }
        }
    } else {
        cm?.run {
            cm.activeNetworkInfo?.run {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    result = 2
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    result = 1
                }
            }
        }
    }
    return result > 0
}

fun <T> LiveData<T>.observeSafe(owner: LifecycleOwner, observer: (T) -> Unit) {
    this.observe(owner, Observer<T> { t ->
        t?.let(observer)
    })
}


fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun convertTimeToDate(time:String) : String{
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.FRANCE)
    val hourFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
    val date = Date(time.toLong())
    val dateString = dateFormat.format(date)
    val hourString = hourFormat.format(date)
    return "Le $dateString à $hourString"
}

fun getCircularProgressDrawable(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    return circularProgressDrawable
}