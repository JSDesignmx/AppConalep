package mx.jsdesign.calificacionesconalep.adapters

import android.content.Context
import android.os.Build
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import mx.jsdesign.calificacionesconalep.R
import mx.jsdesign.calificacionesconalep.formatName
import mx.jsdesign.calificacionesconalep.models.SchoolGrade

class SchoolGradesAdapter(private var context: Context, var mData: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var allData = mutableListOf<Any>()

    private val schoolGradeItemViewType = 0
    private val nativeAdViewType = 1

    inner class SchoolGradesViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val expandableView: ConstraintLayout = itemView.findViewById(R.id.expandableView)
        private val arrowBtn: AppCompatImageButton = itemView.findViewById(R.id.arrowBtn)

        val gradeBg: ImageView = itemView.findViewById(R.id.circleImage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val nombreModulo: TextView = itemView.findViewById(R.id.nombreModulo)
        val grade: TextView = itemView.findViewById(R.id.grade)
        val indicators: TextView = itemView.findViewById(R.id.indicators)
        val percent: TextView = itemView.findViewById(R.id.percent)
        val module: TextView = itemView.findViewById(R.id.module)
        val period: TextView = itemView.findViewById(R.id.period)
        val calendar: TextView = itemView.findViewById(R.id.calendar)
        val teacher: TextView = itemView.findViewById(R.id.teacher)


        init {
            arrowBtn.setOnClickListener {
                if (expandableView.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                    expandableView.visibility = View.VISIBLE
                    arrowBtn.setBackgroundResource(R.drawable.ic_keyboard_arrow_up)
                } else {
                    TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                    expandableView.visibility = View.GONE
                    arrowBtn.setBackgroundResource(R.drawable.ic_keyboard_arrow_down)
                }
            }
        }
    }

    inner class UnifiedNativeAdViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var adView:UnifiedNativeAdView = itemView.findViewById(R.id.ad_view)

        fun getAdView(): UnifiedNativeAdView {
            return adView
        }

        init {
            //adView.mediaView = adView.findViewById<View>(R.id.ad_media) as MediaView

            // Register the view used for each individual asset.
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_icon)
            adView.priceView = adView.findViewById(R.id.ad_price)
            adView.starRatingView = adView.findViewById(R.id.ad_stars)
            adView.storeView = adView.findViewById(R.id.ad_store)
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        }
    }

    init {
        allData.addAll(mData)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            nativeAdViewType -> UnifiedNativeAdViewHolder(LayoutInflater.from(
                viewGroup.context
            ).inflate(
                R.layout.ad_unified,
                viewGroup, false
            ))
            schoolGradeItemViewType -> SchoolGradesViewHolder(LayoutInflater.from(viewGroup.context).inflate(
                R.layout.school_subject_item, viewGroup, false
            ))
            else -> SchoolGradesViewHolder(LayoutInflater.from(viewGroup.context).inflate(
                R.layout.school_subject_item, viewGroup, false
            ))
        }
    }

    override fun getItemViewType(position: Int): Int {

        val recyclerViewItem:Any = mData[position]

        if (recyclerViewItem is UnifiedNativeAd) {
            return nativeAdViewType
        }
        return schoolGradeItemViewType
    }

    override fun getItemCount(): Int = mData.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filteredList = mutableListOf<Any>()
                if (constraint.isEmpty()) filteredList.addAll(mData)
                else allData.filterTo(filteredList) {
                    if(it is SchoolGrade)
                        it.grado == constraint
                    else
                        false
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList

                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                mData.clear()
                @Suppress("UNCHECKED_CAST")
                mData.addAll(results.values as Collection<SchoolGrade>)
                notifyDataSetChanged()
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            nativeAdViewType -> {
                val nativeAd = mData[position] as UnifiedNativeAd
                populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).getAdView())
            }
            schoolGradeItemViewType -> {
                val schoolGradesViewHolder = holder as SchoolGradesViewHolder
                val item = mData[position] as SchoolGrade
                populateSchoolGradeView(item, schoolGradesViewHolder)
            }
        }
    }

    private fun populateSchoolGradeView(item: SchoolGrade, holder:SchoolGradesViewHolder) {
        var gradeBg = R.drawable.background

        val grade = item.calificacionFinal.toDoubleOrNull() ?: 0.0
        val totalIndicators = item.indicadoresModulo.toDoubleOrNull() ?: 1.0
        val evaluatedIndicators = item.indicadoresEvaluadosModulo.toDoubleOrNull() ?: 0.0
        val percent = ((evaluatedIndicators / totalIndicators) * 100).toInt()

        if (grade < 6)
            gradeBg = R.drawable.bg_red
        else if (grade >= 6)
            gradeBg = R.drawable.bg_green

        if (item.indicadoresEvaluadosModulo != item.indicadoresModulo)
            gradeBg = R.drawable.bg_blue

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.progressBar.setProgress(percent, true)
        else
            holder.progressBar.progress = percent

        holder.gradeBg.setImageResource(gradeBg)
        holder.nombreModulo.text = item.nombreModulo
        holder.indicators.text = context.getString(
            R.string.indicators_progress,
            item.indicadoresEvaluadosModulo,
            item.indicadoresModulo
        )
        holder.grade.text =
            if (evaluatedIndicators == totalIndicators) grade.toString() else context.getString(
                R.string.pending_grade
            )
        holder.percent.text = context.getString(R.string.percentage, item.porcentajeAlcanzado)
        holder.module.text = item.claveModulo
        holder.period.text = item.periodoEscolar
        holder.calendar.text = item.calendario
        holder.teacher.text = formatName(item.nombrePSP)
    }

    private fun populateNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        val icon = nativeAd.icon
        if (icon == null) {
            adView.iconView.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.GONE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.GONE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.GONE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.GONE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd)
    }
}