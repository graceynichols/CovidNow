package com.example.covidnow

import android.app.Application
import com.example.covidnow.models.Location
import com.parse.Parse
import com.parse.ParseObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.parceler.Parcel

@Parcel
class ParseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG)

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        val builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.apply { httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY }
        builder.networkInterceptors().add(httpLoggingInterceptor)

        // Register parse models
        ParseObject.registerSubclass(Location::class.java)


        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(Parse.Configuration.Builder(this)
                .applicationId("grace-covidnow") // should correspond to APP_ID env variable
                .clientKey("CodepathParseSecretKey") // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://grace-covidnow.herokuapp.com/parse").build())
    }
}