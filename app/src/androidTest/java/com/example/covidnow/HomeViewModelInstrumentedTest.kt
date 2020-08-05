package com.example.covidnow

import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.models.Location
import com.example.covidnow.viewmodels.HomeViewModel
import com.parse.SaveCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Handler

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class HomeViewModelInstrumentedTest {

    val mViewModel = HomeViewModel(Application())
    private val TAG = "HomeViewModelTest"
    private var jsonLocation: JSONObject? = null
    private var finalLocation: Location?  = null
    @Before
    @Test
    fun testGetAddress() {
        val frag = HomeFragment()
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Test 33 wakefield Dr
        mViewModel.getAddress(appContext.getString(R.string.google_maps_key), Pair.create(33.8065656, -84.3903833))

        //Make sure json location is correct for these coordinates
        jsonLocation = mViewModel.getJsonLocation().getOrAwaitValue()
        Assert.assertEquals("{\"compound_code\":\"RJ45+JR Atlanta, GA, USA\",\"global_code\":\"865QRJ45+JR\"}", jsonLocation?.getString("plus_code").toString())
    }

    @Test
    fun testLocationToIso() {
        val iso = jsonLocation?.let { mViewModel.locationToISO(it) }
        Assert.assertEquals(iso?.first, "US-GA")
        Assert.assertEquals(iso?.second, "Georgia")
    }

    @Test
    fun testGetLocationAsLocation() {
        jsonLocation?.let { mViewModel.getLocationAsLocation(it) }
        finalLocation = mViewModel.getFinalLocation().getOrAwaitValue()
        // Check place Id
        Assert.assertEquals(finalLocation?.placeId, "ChIJ1x--eqwF9YgR5WsRuMYzB5c")
        // Check address
        Assert.assertEquals(finalLocation?.address, "33 Wakefield Dr NE, Atlanta, GA 30309, USA")
    }

    fun <T> LiveData<T>.getOrAwaitValue(
            time: Long = 2,
            timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        val thing = this

        GlobalScope.launch(Dispatchers.Main) {
            thing.observeForever(observer)
        }


        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

}