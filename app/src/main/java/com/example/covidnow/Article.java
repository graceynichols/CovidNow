package com.example.covidnow;

import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Article {
    private String headline;
    private String source;
    private String date;
    private String summary;
    private String url;

    public Article() {};

    public Article(String headline, String source, String summary, String url, String date) {
        this.headline = headline;
        this.source = source;
        this.date = date;
        this.summary = summary;
        this.url = url;
    }

    public static Article fromJson(JSONObject json) throws JSONException {
        Article article = new Article();
        article.headline = json.getString("title");
        article.summary = json.getString("excerpt");
        article.date = json.getString("publishedDateTime");
        article.source = json.getJSONObject("provider").getString("name");
        article.url = json.getString("webUrl");
        return article;
    }

    public String getHeadline() {
        return headline;
    }

    public String getSource() {
        return source;
    }

    public String getDate() {
        return date;
    }

    /*
    public String getStringDate() {
        return getRelativeTimeAgo(date);
    }*/

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(Date date) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        long dateMillis = date.getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        return relativeDate;
    }
}
