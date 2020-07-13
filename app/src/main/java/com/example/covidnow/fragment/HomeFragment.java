package com.example.covidnow.fragment;

import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.covidnow.Article;
import com.example.covidnow.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private static final String TAG = "PostsFragment";
    private static final int POST_LIMIT = 20;
    private RecyclerView rvPosts;
    protected PostsAdapter adapter;
    protected List<Article> allPosts;
    private ProgressBar pb;

    private SupportMapFragment mapFragment;
    private String FIND_PLACE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=";
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    public static JsonHttpResponseHandler.JSON location;

    private final static String KEY_LOCATION = "location";


    public HomeFragment() {
        // Required empty public constructor
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);
        pb = view.findViewById(R.id.pbLoading);

        // Recyclerview setup
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts);
        rvPosts.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(layoutManager);

        // Add lines between recycler view
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvPosts.addItemDecoration(itemDecoration);

        // Setup scrollListener for endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if (allPosts.size() >= POST_LIMIT) {
                    pb.setVisibility(ProgressBar.VISIBLE);
                    loadNextDataFromApi(page);
                    pb.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        };
        // Adds the scroll listener to RecyclerView (for endless scrolling)
        rvPosts.addOnScrollListener(scrollListener);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pb.setVisibility(ProgressBar.VISIBLE);
                fetchTimelineAsync(0);
                pb.setVisibility(ProgressBar.INVISIBLE);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        queryPosts();
    }

    private void loadNextDataFromApi(int page) {
        // Send an API request to retrieve appropriate paginated data
        Log.i(TAG, "Loading next data");
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(POST_LIMIT);
        query.addDescendingOrder(Post.KEY_CREATED_KEY);
        // Make sure to only get posts older than your oldest post
        query.whereLessThan(Post.KEY_CREATED_KEY, allPosts.get(POST_LIMIT * (page) - 1).getCreatedAt());
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + " Username: " + post.getUser().getUsername());
                }
                // Add new data to recycler view
                allPosts.addAll(posts);
                Log.i(TAG, "" + allPosts.size());
                adapter.notifyDataSetChanged();
                Log.i(TAG, "Query older posts finished");
            }
        });
    }

    protected void queryPosts() {
        pb.setVisibility(ProgressBar.VISIBLE);
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        // Query the newest 20 posts from Parse
        query.include(Post.KEY_USER);
        query.setLimit(POST_LIMIT);
        query.addDescendingOrder(Post.KEY_CREATED_KEY);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + " Username: " + post.getUser().getUsername());
                }
                // Add data to recyclerview
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                scrollListener.resetState();
                pb.setVisibility(ProgressBar.INVISIBLE);
                Log.i(TAG, "Query posts finished");
            }
        });
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        adapter.clear();
        queryPosts();
        swipeContainer.setRefreshing(false);
    }
}