package mx.jsdesign.calificacionesconalep.ui

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import mx.jsdesign.calificacionesconalep.JsApp
import mx.jsdesign.calificacionesconalep.R
import mx.jsdesign.calificacionesconalep.adapters.SchoolGradesAdapter
import mx.jsdesign.calificacionesconalep.getSchoolGradeData
import mx.jsdesign.calificacionesconalep.models.SchoolGrade
import mx.jsdesign.calificacionesconalep.network.ConalepWs

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var schoolGradesAdapter: SchoolGradesAdapter? = null
    private lateinit var adView: AdView
    private var backtoast: Toast? = null

    private var adsNumber = 2

    private val mRecyclerViewItems: MutableList<Any> = mutableListOf()
    private lateinit var adLoader: AdLoader
    private var mNativeAds: MutableList<UnifiedNativeAd> = mutableListOf()

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            val adWidthPixels = outMetrics.widthPixels.toFloat()

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @UnstableDefault
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportActionBar?.title = ""

        adsNumber = JsApp.remoteConfig[getString(R.string.number_of_ads_in_list)].asLong().toInt()

        loadBottomBanner()

        setStudentData()

        handleIntent()

        fab.setOnClickListener { startActivity(Intent(this, CalendarActivity::class.java)) }

        buttonRetry.setOnClickListener { getData() }
    }

    private fun setStudentData() {
        txtStudentName.text = JsApp.studentName
        txtStudentId.text = JsApp.studentId
        txtStudentSchool.text = JsApp.studentSchool
    }

    @UnstableDefault
    private fun handleIntent() {
        val data = intent.getSchoolGradeData()

        if (data != null)
            displayData(data)
        else
            getData()
    }

    @UnstableDefault
    private fun getData() {
        showLoading()
        CoroutineScope(Dispatchers.Main).launch {

            val response = ConalepWs.getSchoolGrades(JsApp.studentId, this@MainActivity)

            if (response.success)
                response.schoolGrades?.let { displayData(it) }
            else
                response.message?.let { displayError(it) }
        }
    }

    private fun displayData(schoolGrades: List<SchoolGrade>) {

        mRecyclerViewItems.addAll(schoolGrades)

        schoolGradesAdapter = SchoolGradesAdapter(this, mRecyclerViewItems)

        schoolGradesRv.adapter = schoolGradesAdapter
        schoolGradesRv.layoutManager =
            GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false)

        fillSpinner(schoolGrades)

        schoolGradesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) fab.hide() else fab.show()
            }
        })

        hideLoading()
    }

    private fun fillSpinner(schoolGrades: List<SchoolGrade>) {
        val gradesList: MutableList<String> = mutableListOf()

        schoolGrades.groupBy { it.grado }.forEach {
            gradesList += "${it.key}${getString(R.string.spinner_option_label)}"
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gradesList).also {
            spinner.adapter = it
        }

        spinner.onItemSelectedListener = this
    }

    private fun displayError(error: String) {

        txtError.text = getString(R.string.error_getting_remote_config, error)
        hideLoading(false)
    }

    private fun showLoading() {
        txtError.text = ""
        spinner.visibility = View.GONE
        schoolGradesRv.visibility = View.GONE
        txtError.visibility = View.GONE
        buttonRetry.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading(success: Boolean = true) {
        progressBar.visibility = View.GONE

        if (success) {
            schoolGradesRv.visibility = View.VISIBLE
            spinner.visibility = View.VISIBLE
        } else {
            txtError.visibility = View.VISIBLE
            buttonRetry.visibility = View.VISIBLE
        }
    }

    private fun loadBottomBanner() {
        if (!JsApp.remoteConfig[getString(R.string.show_main_bottom_ad)].asBoolean()) return
        if (JsApp.remoteConfig[getString(R.string.ad_free_user)].asString() == JsApp.studentId) return
        adView = AdView(this)
        ad_view_container.addView(adView)
        adView.adUnitId =
            JsApp.remoteConfig[getString(R.string.main_banner_bottom_ad_unit_id)].asString()
        adView.adSize = adSize
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun insertAdsInMenuItems() {
        if (mNativeAds.size <= 0) {
            return
        }

        if (mNativeAds.size > adsNumber)
            mNativeAds = mNativeAds.subList(0, adsNumber)

        val offset = mRecyclerViewItems.size / mNativeAds.size + 2
        var index = 0

        for (ad in mNativeAds) {
            if (mRecyclerViewItems.size >= index)
                mRecyclerViewItems.add(index, ad)
            index += offset
        }
        schoolGradesAdapter?.notifyDataSetChanged()
    }

    private fun loadNativeAds() {

        if (!JsApp.remoteConfig[getString(R.string.show_list_adds)].asBoolean()) return
        if (JsApp.remoteConfig[getString(R.string.ad_free_user)].asString() == JsApp.studentId) return

        mNativeAds.clear()

        adLoader = AdLoader.Builder(
                this,
                JsApp.remoteConfig[getString(R.string.native_ad_unit_id)].asString()
            )
            .forUnifiedNativeAd {
                mNativeAds.add(it)
                if (!adLoader.isLoading) insertAdsInMenuItems()
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    if (!adLoader.isLoading) insertAdsInMenuItems()
                }
            }).build()

        // Load the Native Express ad.
        adLoader.loadAds(AdRequest.Builder().build(), adsNumber)
    }

    override fun onBackPressed() {
        if (backtoast != null && backtoast?.view?.windowToken != null) {
            backtoast?.cancel()
            super.onBackPressed()
        } else {
            backtoast = Toast.makeText(
                this,
                getString(R.string.press_back_again_to_exit),
                Toast.LENGTH_SHORT
            )
            backtoast?.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                JsApp.logout()
                startActivity(Intent(this, SplashScreen::class.java))
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        schoolGradesAdapter?.filter?.filter(
            (parent.selectedItem as String).replace(
                getString(R.string.spinner_option_label),
                ""
            )
        )
        loadNativeAds()
    }

}
