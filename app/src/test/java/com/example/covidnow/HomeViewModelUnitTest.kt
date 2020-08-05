package com.example.covidnow

import android.R
import android.app.Application
import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.viewmodels.HomeViewModel
import kotlinx.coroutines.runBlocking
import okhttp3.internal.waitMillis
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class HomeViewModelUnitTest {
    private val mViewModel = HomeViewModel(Application())
    private val TAG = "HomeViewModelUnitTest"
    val KEY = "AIzaSyDveUqNL9WHKo5vXNlnGfa-00tJukplXEU"
    //private val apiKey = mViewModel.g

    @Mock
    private lateinit var mockContext: Context

    @Test
    @Throws(JSONException::class)
    fun <T> testGetAddress() {
        // Listen for response from geocoding API to give to news API
        val address = runBlocking {
            mViewModel.getAddress(KEY, Pair.create(33.8065656, -84.3903833))
        }

        println(mViewModel.getJsonLocation().value?.getString("plus_code").toString())
        assert(mViewModel.getJsonLocation().value?.getString("plus_code").toString() == "{\"compound_code\":\"CWC8+Q9 Mountain View, CA, USA\",\"global_code\":\"849VCWC8+Q9\"}")

    }
}
