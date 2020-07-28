package com.example.covidnow

import android.app.Application
import com.example.covidnow.viewmodels.HomeViewModel
import org.json.JSONException
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class HomeViewModelUnitTest {
    private val mViewModel = HomeViewModel(Application())

    @Test
    @Throws(JSONException::class)
    fun testLocationToIso() {
        //Gson gson = new Gson();
        //new JSONObject(.)

        //JSONObject jsonLocation = new JSONObject();
        //      ("{\"plus_code\":{\"compound_code\":\"CWC8+Q9 Mountain View, CA, USA\",\"global_code\":\"849VCWC8+Q9\"},\"results\":[{\"address_components\":[{\"long_name\":\"1600\",\"short_name\":\"1600\",\"types\":[\"street_number\"]},{\"long_name\":\"Amphitheatre Parkway\",\"short_name\":\"Amphitheatre Pkwy\",\"types\":[\"route\"]},{\"long_name\":\"Mountain View\",\"short_name\":\"Mountain View\",\"types\":[\"locality\",\"political\"]},{\"long_name\":\"Santa Clara County\",\"short_name\":\"Santa Clara County\",\"types\":[\"administrative_area_level_2\",\"political\"]},{\"long_name\":\"California\",\"short_name\":\"CA\",\"types\":[\"administrative_area_level_1\",\"political\"]},{\"long_name\":\"United States\",\"short_name\":\"US\",\"types\":[\"country\",\"political\"]},{\"long_name\":\"94043\",\"short_name\":\"94043\",\"types\":[\"postal_code\"]}],\"formatted_address\":\"1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA\",\"geometry\":{\"location\":{\"lat\":37.4219999,\"lng\":-122.0840575},\"location_type\":\"ROOFTOP\",\"viewport\":{\"northeast\":{\"lat\":37.4233488802915,\"lng\":-122.0827085197085},\"southwest\":{\"lat\":37.4206509197085,\"lng\":-122.0854064802915}}},\"place_id\":\"ChIJj61dQgK6j4AR4GeTYWZsKWw\",\"plus_code\":{\"compound_code\":\"CWC8+Q9 Mountain View, CA, USA\",\"global_code\":\"849VCWC8+Q9\"},\"types\":[\"establishment\",\"point_of_interest\"]},{\"address_components\":[{\"long_name\":\"1600\",\"short_name\":\"1600\",\"types\":[\"street_number\"]},{\"long_name\":\"Amphitheatre Parkway\",\"short_name\":\"Amphitheatre Pkwy\",\"types\":[\"route\"]},{\"long_name\":\"Mountain View\",\"short_name\":\"Mountain View\",\"types\":[\"locality\",\"political\"]},{\"long_name\":\"Santa Clara County\",\"short_name\":\"Santa Clara County\",\"types\":[\"administrative_area_level_2\",\"political\"]},{\"long_name\":\"California\",\"short_name\":\"CA\",\"types\":[\"administrative_area_level_1\",\"political\"]},{\"long_name\":\"United States\",\"short_name\":\"US\",\"types\":[\"country\",\"political\"]},{\"long_name\":\"94043\",\"short_name\":\"94043\",\"types\":[\"postal_code\"]}],\"formatted_address\":\"1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA\",\"geometry\":{\"location\":{\"lat\":37.4220578,\"lng\":-122.0840897},\"location_type\":\"ROOFTOP\",\"viewport\":{\"northeast\":{\"lat\":37.4234067802915,\"lng\":-122.0827407197085},\"southwest\":{\"lat\":37.4207088197085,\"lng\":-122.0854386802915}}},\"place_id\":\"ChIJtYuu0V25j4ARwu5e4wwRYgE\",\"plus_code\":{\"compound_code\":\"CWC8+R9 Mountain View, CA, USA\",\"global_code\":\"849VCWC8+R9\"},\"types\":[\"street_address\"]},{\"address_components\":[{\"long_name\":\"Google Building 40\",\"short_name\":\"Google Building 40\",\"types\":[\"premise\"]},{\"long_name\":\"1600\",\"short_name\":\"1600\",\"types\":[\"street_number\"]},{\"long_name\":\"Amphitheatre Parkway\",\"short_name\":\"Amphitheatre Pkwy\",\"types\":[\"route\"]},{\"long_name\":\"Mountain View\",\"short_name\":\"Mountain View\",\"types\":[\"locality\",\"political\"]},{\"long_name\":\"Santa Clara County\",\"short_name\":\"Santa Clara County\",\"types\":[\"administrative_area_level_2\",\"political\"]},{\"long_name\":\"California\",\"short_name\":\"CA\",\"types\":[\"administrative_area_level_1\",\"political\"]},{\"long_name\":\"United States\",\"short_name\":\"US\",\"types\":[\"country\",\"political\"]},{\"long_name\":\"94043\",\"short_name\":\"94043\",\"types\":[\"postal_code\"]}],\"formatted_address\":\"Google Building 40, 1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA\",\"geometry\":{\"bounds\":{\"northeast\":{\"lat\":37.4226621,\"lng\":-122.0829306},\"southwest\":{\"lat\":37.4220703,\"lng\":-122.0849584}},\"location\":{\"lat\":37.422388,\"lng\":-122.0841883},\"location_type\":\"ROOFTOP\",\"viewport\":{\"northeast\":{\"lat\":37.4237151802915,\"lng\":-122.0825955197085},\"southwest\":{\"lat\":37.4210172197085,\"lng\":-122.0852934802915}}},\"place_id\":\"ChIJj38IfwK6j4ARNcyPDnEGa9g\",\"types\":[\"premise\"]},{\"address_components\":[{\"long_name\":\"Unnamed Road\",\"short_name\":\"Unnamed Road\",\"types\":[\"route\"]},{\"long_name\":\"Mountain View\",\"short_name\":\"Mountain View\",\"types\":[\"locality\",\"political\"]},{\"long_name\":\"Santa Clara County\",\"short_name\":\"Santa Clara County\",\"types\":[\"administrative_area_level_2\",\"political\"]},{\"long_name\":\"California\",\"short_name\":\"CA\",\"types\":[\"administrative_area_level_1\",\"political\"]},{\"long_name\":\"United States\",\"short_name\":\"US\",\"types\":[\"country\",\"political\"]}");
        //Pair<String, String> iso = mViewModel.locationToISO(jsonLocation);
        //assertEquals(iso.first, "US-CA");
        //assertEquals(iso.second, "California");
    }
}