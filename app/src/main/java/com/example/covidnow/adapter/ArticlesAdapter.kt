package com.example.covidnow.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.covidnow.R
import com.example.covidnow.fragment.ArticleDetailsFragment
import com.example.covidnow.models.Article
import org.parceler.Parcels


class ArticlesAdapter(private val fragment: Fragment, private val articles: MutableList<Article>) : RecyclerView.Adapter<ArticlesAdapter.ViewHolder>() {
    private val context: Context? = fragment.context

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeadline: TextView = itemView.findViewById(R.id.tvHeadline)
        private val tvSource: TextView = itemView.findViewById(R.id.tvSource)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnShare: ImageView = itemView.findViewById(R.id.btnShare)
        fun bind(article: Article) {
            tvHeadline.text = article.headline
            tvSource.text = article.source
            tvDate.text = article.date

            // Listen for share button
            btnShare.setOnClickListener {
                // Share article URL
                article.url?.let { it1 -> share(it1) }
            }

            // On click for article details view
            itemView.setOnClickListener {
                val result = Bundle()
                result.putParcelable("article", Parcels.wrap(article))
                // Start article details fragment
                val newFrag: Fragment = ArticleDetailsFragment()
                newFrag.arguments = result
                //fragment.findNavController().navigate(R.id.action_global_articleDetailsFragment, result)
                fragment.fragmentManager?.beginTransaction()?.replace(R.id.flContainer,
                       newFrag)?.addToBackStack("HomeFragment")?.commit()
            }
        }

        private fun share(link: String) {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this article on COVID-19!")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, link)
            if (context != null) {
                startActivity(context, Intent.createChooser(sharingIntent, "Share via"), null)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    fun addAll(news: List<Article>) {
        articles.addAll(news)
        notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "ArticlesAdapter"
    }

}