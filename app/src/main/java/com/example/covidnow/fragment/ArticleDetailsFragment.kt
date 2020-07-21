package com.example.covidnow.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.covidnow.R
import com.example.covidnow.models.Article
import org.parceler.Parcels

class ArticleDetailsFragment : Fragment() {
    private var article: Article? = null
    private var tvHeadline: TextView? = null
    private var tvSource: TextView? = null
    private var tvDate: TextView? = null
    private var tvSummary: TextView? = null

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article_details, parent, false)
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        article = Parcels.unwrap<Article>(arguments?.getParcelable("article"))
        tvHeadline = view.findViewById(R.id.tvHeadline)
        tvSource = view.findViewById(R.id.tvSource)
        tvDate = view.findViewById(R.id.tvDate)
        tvSummary = view.findViewById(R.id.tvSummary)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)

        // Set text information
        tvHeadline?.text = article?.headline
        tvSource?.text = article?.source
        tvDate?.text = article?.date
        tvSummary?.text = article?.summary

        // Add image if there is one
        if (article?.imageUrl != null) {
            ivImage.visibility = View.VISIBLE
            val lp = tvSummary?.layoutParams as RelativeLayout.LayoutParams
            ivImage.id.let { lp.addRule(RelativeLayout.BELOW, it) }
            Glide.with(this).load(article?.imageUrl).centerCrop().into(ivImage)
        }

        // Clicking headline takes you to article on internet
        tvHeadline?.setOnClickListener(View.OnClickListener {
            val uri = Uri.parse(article?.url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })
    }
}