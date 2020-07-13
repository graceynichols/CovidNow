package com.example.covidnow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.covidnow.fragment.ArticleDetailsFragment;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    private static final String TAG="ArticlesAdapter";
    private Context context;
    private Fragment fragment;
    private List<Article> articles;

    public ArticlesAdapter(Fragment fragment,List<Article> articles) {
            this.fragment = fragment;
            this.context = fragment.getContext();
            this.articles = articles;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHeadline;
        private TextView tvSource;
        private TextView tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeadline = itemView.findViewById(R.id.tvHeadline);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(final Article article) {
            tvHeadline.setText(article.getHeadline());
            tvSource.setText(article.getSource());
            tvDate.setText(article.getDate());

            // On click for article details view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle result = new Bundle();
                    result.putParcelable("article", Parcels.wrap(article));
                    // Start article details fragment
                    Fragment newFrag = new ArticleDetailsFragment();
                    newFrag.setArguments(result);
                    fragment.getFragmentManager().beginTransaction().replace(R.id.flContainer,
                            newFrag).commit();
                }
            });
        }

    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        articles.clear();
        notifyDataSetChanged();
    }
}
