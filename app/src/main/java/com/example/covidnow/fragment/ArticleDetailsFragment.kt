package com.example.covidnow.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.covidnow.R
import com.example.covidnow.activity.MainActivity
import com.example.covidnow.models.Article
import org.parceler.Parcels


class ArticleDetailsFragment : Fragment() {
    private val TAG: String = "ArticleDetailsFragment"
    private var article: Article? = null
    private var tvHeadline: TextView? = null
    private var tvSource: TextView? = null
    private var tvDate: TextView? = null
    private var tvSummary: TextView? = null
    private var pbLoading: ProgressBar? = null
    private var btnShare: ImageView? = null
    private var btnLink: ImageView? = null
    private var toolbar: android.widget.Toolbar? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article_details, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        // Unwrap article from arguments
        article = Parcels.unwrap<Article>(arguments?.getParcelable("article"))
        tvHeadline = view.findViewById(R.id.tvHeadline)
        tvSource = view.findViewById(R.id.tvSource)
        tvDate = view.findViewById(R.id.tvDate)
        tvSummary = view.findViewById(R.id.tvSummary)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        toolbar = view.findViewById(R.id.my_toolbar)
        pbLoading = view.findViewById(R.id.pbLoading)

        // Set up custom toolbar
        article?.url?.let { setupToolbar(it) }

        // Show progress bar while loading
        pbLoading?.visibility = View.VISIBLE

        // Set text information
        tvHeadline?.text = article?.headline
        tvSource?.text = article?.source
        tvDate?.text = article?.date
        tvSummary?.text = article?.summary

        // Add image if there is one
        if (article?.imageUrl != null) {
            ivImage.visibility = View.VISIBLE
            val lp = tvHeadline?.layoutParams as RelativeLayout.LayoutParams
            ivImage.id.let { lp.addRule(RelativeLayout.BELOW, it) }
            Glide.with(this).load(article?.imageUrl).centerCrop().into(ivImage)
        }

        // Clicking headline takes you to article on internet
        tvHeadline?.setOnClickListener(View.OnClickListener {
            val uri = Uri.parse(article?.url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })

        // Hide progress bar
        pbLoading?.visibility = View.GONE
    }

    private fun setupToolbar(link: String) {
        (activity as MainActivity).hideActionBar()
        //toolbar?.inflateMenu(R.menu.article_details_toolbar)
        toolbar?.let { (activity as MainActivity).setActionBar(it) }
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar?.setNavigationOnClickListener {
            Log.i(TAG, "Back button clicked")
            goHome()
        }
       // toolbar?

        btnShare = view?.findViewById(R.id.btnShare2)
        btnLink = view?.findViewById(R.id.btnLink2)
        // Hide CovidNow vanity action bar
        btnShare?.setOnClickListener {
            share(link)
        }
        btnLink?.setOnClickListener {
            openInBrowser(link)
        }
    }

    private fun openInBrowser(link: String) {
        // Go to this link in browser
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (context != null) {
            ContextCompat.startActivity(context as Context, intent, null)
        }
    }

    private fun share(link: String) {
        // Share this article link
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sharing_body))
        sharingIntent.putExtra(Intent.EXTRA_TEXT, link)
        if (context != null) {
            ContextCompat.startActivity(context as Context, Intent.createChooser(sharingIntent, "Share via"), null)
        }
    }

    private fun goHome() {
        (activity as MainActivity).showActionBar()
        Log.i(TAG, "Going home")
        fragmentManager?.popBackStackImmediate()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return  inflater.inflate(R.menu.article_details_toolbar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_link -> {
            // User chose link in browser button
            Log.i(TAG, "Link button pressed")
            true
        }

        R.id.action_share -> {
            // User chose share button
            Log.i(TAG, "Share button pressed")
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


}