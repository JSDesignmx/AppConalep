package mx.jsdesign.calificacionesconalep.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.remoteconfig.ktx.get
import mx.jsdesign.aboutpage.AboutPage
import mx.jsdesign.aboutpage.Element
import mx.jsdesign.calificacionesconalep.BuildConfig
import mx.jsdesign.calificacionesconalep.JsApp
import mx.jsdesign.calificacionesconalep.R

class AboutActivity : AppCompatActivity() {

    private lateinit var aboutPage: View

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupAboutPage()
        setContentView(aboutPage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadBanner()
    }

    private fun setupAboutPage() {
        aboutPage = AboutPage(this)
            .setDescription(getString(R.string.about_app, getString(R.string.app_name)))
            .setImage(R.drawable.ic_launcher_foreground)
            .addItem(Element().setTitle(getString(R.string.version, BuildConfig.VERSION_NAME)))
            .addPlayStore(packageName)
            .addGroup(getString(R.string.contact))
            .addEmail(JsApp.remoteConfig[getString(R.string.contact_email)].asString())
            .addWebsite(JsApp.remoteConfig[getString(R.string.website_url)].asString())
            .addFacebook(JsApp.remoteConfig[getString(R.string.fb_id)].asString())
            .addTwitter(JsApp.remoteConfig[getString(R.string.tw_id)].asString())
            .addInstagram(JsApp.remoteConfig[getString(R.string.ig_id)].asString())
            .addGroup(getString(R.string.legal))
            .addItem(getOssLicenses())
            .addItem(getPrivacyPolicy())
            .addItem(getTermsAndContitions())
            .create()
    }

    private fun loadBanner() {
        if (!JsApp.remoteConfig[getString(R.string.show_calendar_bottom_add)].asBoolean()) return

        if (JsApp.remoteConfig[getString(R.string.ad_free_user)].asString() == JsApp.studentId) return

        val adView = AdView(this)
        val adContainer =
            aboutPage.findViewById<FrameLayout>(mx.jsdesign.R.id.about_ad_view_container)
        adContainer.addView(adView)
        adView.adUnitId =
            JsApp.remoteConfig[getString(R.string.about_banner_bottom_ad_unit_id)].asString()
        adView.adSize = adSize
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun getOssLicenses(): Element {
        val ossLicensesElement = Element()
        ossLicensesElement.title = getString(R.string.open_source_licenses)
        ossLicensesElement.iconDrawable = R.drawable.ic_open_source_initiative
        ossLicensesElement.iconTint = mx.jsdesign.R.color.about_item_icon_color
        ossLicensesElement.onClickListener =
            View.OnClickListener {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses))
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
        return ossLicensesElement
    }

    private fun getPrivacyPolicy(): Element {
        val privacyPolicyElement = Element()
        privacyPolicyElement.title = getString(R.string.privacy_policy)
        privacyPolicyElement.iconDrawable = R.drawable.ic_privacy_policy
        privacyPolicyElement.iconTint = mx.jsdesign.R.color.about_item_icon_color
        privacyPolicyElement.intent = Intent(
            Intent.ACTION_VIEW, Uri.parse(
                JsApp.remoteConfig[getString(
                    R.string.privacy_policy_url
                )].asString()
            )
        )
        return privacyPolicyElement
    }

    private fun getTermsAndContitions(): Element {
        val termsAndContitionsElement = Element()
        termsAndContitionsElement.title = getString(R.string.terms_and_conditions)
        termsAndContitionsElement.iconDrawable = R.drawable.ic_terms_and_conditions
        termsAndContitionsElement.iconTint = mx.jsdesign.R.color.about_item_icon_color
        termsAndContitionsElement.intent = Intent(
            Intent.ACTION_VIEW, Uri.parse(
                JsApp.remoteConfig[getString(
                    R.string.terms_and_conditions_url
                )].asString()
            )
        )
        return termsAndContitionsElement
    }
}
