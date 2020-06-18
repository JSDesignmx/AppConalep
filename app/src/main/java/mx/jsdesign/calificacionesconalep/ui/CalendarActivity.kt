package mx.jsdesign.calificacionesconalep.ui

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.activity_calendar.*
import mx.jsdesign.calificacionesconalep.JsApp
import mx.jsdesign.calificacionesconalep.R

class CalendarActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var bottomAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadTopBanner()
        loadBottomBanner()

        loadCalendar()
    }

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

    private fun loadCalendar() {
        webView.loadDataWithBaseURL(
            getString(R.string.assets_path),
            getString(R.string.calendar_html_source),
            getString(R.string.html_mime_type),
            getString(R.string.html_encoding),
            null
        )
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
    }

    private fun loadTopBanner() {
        if(!JsApp.remoteConfig[getString(R.string.show_calendar_top_add)].asBoolean()) return
        if (JsApp.remoteConfig[getString(R.string.ad_free_user)].asString() == JsApp.studentId) return
        adView = AdView(this)
        top_ad_view_container.addView(adView)
        adView.adUnitId = JsApp.remoteConfig[getString(R.string.calendar_banner_top_ad_unit_id)].asString()
        adView.adSize = adSize
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun loadBottomBanner() {
        if(!JsApp.remoteConfig[getString(R.string.show_calendar_bottom_add)].asBoolean()) return
        if (JsApp.remoteConfig[getString(R.string.ad_free_user)].asString() == JsApp.studentId) return
        bottomAdView = AdView(this)
        bottom_ad_view_container.addView(bottomAdView)
        bottomAdView.adUnitId = JsApp.remoteConfig[getString(R.string.calendar_banner_bottom_ad_unit_id)].asString()
        bottomAdView.adSize = adSize
        bottomAdView.loadAd(AdRequest.Builder().build())
    }
}
