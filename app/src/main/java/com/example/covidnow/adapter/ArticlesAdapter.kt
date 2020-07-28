package com.example.covidnow.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
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
        fun bind(article: Article) {
            tvHeadline.text = article.headline
            tvSource.text = article.source
            tvDate.text = article.date

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