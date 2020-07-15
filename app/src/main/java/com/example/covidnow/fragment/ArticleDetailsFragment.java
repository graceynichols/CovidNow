package com.example.covidnow.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.covidnow.Article;
import com.example.covidnow.R;
import android.widget.RelativeLayout.LayoutParams;

import org.parceler.Parcels;

public class ArticleDetailsFragment extends Fragment {
    private Article article;
    private TextView tvHeadline;
    private TextView tvSource;
    private TextView tvDate;
    private TextView tvSummary;
    private ImageView ivImage;


    public ArticleDetailsFragment() {
        // Required empty public constructor
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_details, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        article = Parcels.unwrap(getArguments().getParcelable("article"));
        tvHeadline = view.findViewById(R.id.tvHeadline);
        tvSource = view.findViewById(R.id.tvSource);
        tvDate = view.findViewById(R.id.tvDate);
        tvSummary = view.findViewById(R.id.tvSummary);
        ivImage = view.findViewById(R.id.ivImage);

        // Set text information
        tvHeadline.setText(article.getHeadline());
        tvSource.setText(article.getSource());
        tvDate.setText(article.getDate());
        tvSummary.setText(article.getSummary());

        // Add image if there is one
        if (article.getImageUrl() != null) {
            ivImage.setVisibility(View.VISIBLE);
            LayoutParams lp = (LayoutParams) tvSummary.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ivImage.getId());
            Glide.with(this).load(article.getImageUrl()).centerCrop().into(ivImage);
        }

        // Clicking headline takes you to article on internet
        tvHeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(article.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
}