package mx.jsdesign.calificacionesconalep

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class JsApp : Application() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        remoteConfig = Firebase.remoteConfig

        PREFS_FILENAME = "${applicationContext.packageName}.prefs"
        prefs = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        MobileAds.initialize(this)

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    companion object {

        lateinit var remoteConfig: FirebaseRemoteConfig

        private const val STUDENT_NAME = "STUDENT_NAME"
        private const val STUDENT_ID = "STUDENT_ID"
        private const val STUDENT_TITLE = "STUDENT_TITLE"
        private const val STUDENT_SCHOOL = "STUDENT_SCHOOL"
        private const val EMPTY_VALUE = ""
        private var prefs: SharedPreferences? = null
        private lateinit var PREFS_FILENAME: String

        var studentName: String
            get() = prefs?.getString(STUDENT_NAME, EMPTY_VALUE) ?: ""
            set(value) = prefs?.edit()?.putString(STUDENT_NAME, value)?.apply()!!

        var studentId: String
            get() = prefs?.getString(STUDENT_ID, EMPTY_VALUE) ?: ""
            set(value) = prefs?.edit()?.putString(STUDENT_ID, value)?.apply()!!

        var studentTitle: String
            get() = prefs?.getString(STUDENT_TITLE, EMPTY_VALUE) ?: ""
            set(value) = prefs?.edit()?.putString(STUDENT_TITLE, value)?.apply()!!

        var studentSchool: String
            get() = prefs?.getString(STUDENT_SCHOOL, EMPTY_VALUE) ?: ""
            set(value) = prefs?.edit()?.putString(STUDENT_SCHOOL, value)?.apply()!!

        fun logout() =
            prefs?.edit()?.remove(STUDENT_NAME)?.remove(STUDENT_ID)?.remove(STUDENT_TITLE)?.remove(STUDENT_SCHOOL)?.apply()

        fun isValidStudent() =
            studentName.isNotEmpty() && studentId.isNotEmpty() && studentTitle.isNotEmpty() && studentSchool.isNotEmpty()
    }
}