package mx.jsdesign.calificacionesconalep.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import mx.jsdesign.calificacionesconalep.*
import mx.jsdesign.calificacionesconalep.models.SchoolGrade
import mx.jsdesign.calificacionesconalep.network.ConalepWs
import java.util.*

class SplashScreen : AppCompatActivity() {

    @UnstableDefault
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        version.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        retryBtn.setOnClickListener { getRemoteConfig() }
        getRemoteConfig()
    }

    @UnstableDefault
    private fun getRemoteConfig() {

        if (retryBtn.visibility == View.VISIBLE)
            retryBtn.visibility = View.GONE

        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))

        JsApp.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkforUpdate()
            } else {
                var error = getString(R.string.error_fetching_data_please_retry)
                task.exception?.let { error = it.message ?: error }
                showRestartButton(error)
            }
        }.addOnFailureListener { e: Exception ->
            FirebaseCrashlytics.getInstance().recordException(e)
            showRestartButton(e.message ?: getString(R.string.error_fetching_data_please_retry))
        }
    }

    private fun showRestartButton(message: String) {
        logo.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.pulse_one_time))
        retryBtn.visibility = View.VISIBLE
        displayError(getString(R.string.error_getting_remote_config, message))
    }

    @UnstableDefault
    private fun checkforUpdate() {
        logo.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.pulse_one_time))

        val latestVersion = JsApp.remoteConfig["latest_version_code"].asLong().toInt()
        if (latestVersion > BuildConfig.VERSION_CODE) {

            val forceUpdate = JsApp.remoteConfig["is_breaking_update"].asBoolean()
            val cancelAction = if (forceUpdate) getString(R.string.close_app) else getString(R.string.later)

            AlertDialog.Builder(this).setTitle(getString(R.string.update_app_title))
                .setMessage(getString(R.string.update_app_body))
                .setPositiveButton(getString(R.string.update_action)) { _, _ ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(this)
                    }
                    this.finishAndRemoveTask()
                }
                .setNegativeButton(cancelAction) { _, _ ->
                    if (forceUpdate) this.finishAndRemoveTask() else validateSession() }
                .setCancelable(false).show()

        } else {
            validateSession()
        }
    }

    @UnstableDefault
    private fun validateSession() = if (JsApp.isValidStudent())
        goToHome()
    else
        showLoginForm()

    @UnstableDefault
    private fun goToHome(data: List<SchoolGrade>? = null) {
        val intent = Intent(this, MainActivity::class.java)

        if (data != null) {
            JsApp.studentName = formatName(data[0].nombreAlumno).toUpperCase(Locale.getDefault())
            JsApp.studentId = data[0].matriculaAlumno
            JsApp.studentTitle = data[0].nombrePlanEstudio
            JsApp.studentSchool = data[0].nombreUnidadAdministrativa
            intent.putSchoolGradeData(data)
        } else
            logo.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))

        startActivity(intent)
        finish()
    }

    @UnstableDefault
    private fun showLoginForm() {

        val animFadeOut = AnimationUtils.loadAnimation(applicationContext, R.anim.translate_up)
        animFadeOut.fillAfter = true

        logo.startAnimation(animFadeOut)
        login_form.visibility = View.VISIBLE
        login_form.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.translate_bottom_center
            )
        )

        loginBtn.setOnClickListener {
            login()
        }
    }

    @UnstableDefault
    private fun login() {
        val studentIdValue = studentId.text.trim().toString()

        if (studentIdValue.isEmpty()) {
            displayError(getString(R.string.studentId_missing))
            return
        }

        loginBtn.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {

            val response = ConalepWs.getSchoolGrades(studentIdValue, this@SplashScreen)

            progressBar.visibility = View.GONE
            loginBtn.visibility = View.VISIBLE

            if (response.success)
                response.schoolGrades?.let { goToHome(it) }
            else
                response.message?.let { displayError(it) }
        }
    }

    private fun displayError(error: String) = Toast.makeText(this, error, Toast.LENGTH_LONG).show()
}