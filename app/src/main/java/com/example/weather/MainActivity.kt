package com.example.weather


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.RvAdapter
import com.example.weather.data.forecastModels.ForecastData
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.databinding.BottomSheetBinding

import com.example.weather.utils.retrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sheetLayoutBinding: BottomSheetBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var city: String = "sao paulo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sheetLayoutBinding = BottomSheetBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)

        dialog.setContentView(sheetLayoutBinding.root)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query!= null){
                    city = query
                }
                getCurrentWeather(city)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        getCurrentWeather(city)

        binding.textForecast.setOnClickListener{
            openDialog()
        }
    }

    private fun openDialog() {
       getForecast()
        sheetLayoutBinding.forecast.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(
                this@MainActivity, 1, RecyclerView.HORIZONTAL, false
            )
        }

        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    private fun getForecast() {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                retrofitInstance.api.getForecast(
                    city,
                    "metric",
                    applicationContext.getString(R.string.api_key)
                )
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            if(response.isSuccessful && response.body()!= null){
                withContext(Dispatchers.Main){
                    val data = response.body()!!

                    var forecastArray = data.list as ArrayList<ForecastData>

                    val adapter = RvAdapter(forecastArray)
                    sheetLayoutBinding.forecast.adapter = adapter
                    sheetLayoutBinding.textSheet.text = "Five days forecast in ${data.city.name}"
                }
            }
        }
    }

    private fun getCurrentWeather(city: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                retrofitInstance.api.getCurrentWeather(this@MainActivity.city, "metric", applicationContext.getString(R.string.api_key))
            }
            catch (e: IOException){
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT).show()
                return@launch
            }
            catch (e: HttpException){
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if(response.isSuccessful && response.body()!= null) {
                withContext(Dispatchers.Main){
                 val data = response.body()!!

                    val iconId = data.weather[0].icon
                    val imgUrl = "https://openweathermap.org/img/wn/$iconId.png"

                    Picasso.get().load(imgUrl).into(binding.imgWeather)

                    binding.textSunrise.text = SimpleDateFormat(
                        "hh:mm a",
                        Locale.ENGLISH
                    ).format(data.sys.sunrise * 1000)

                    binding.textSunset.text = SimpleDateFormat(
                        "hh:mm a",
                        Locale.ENGLISH
                    ).format(data.sys.sunset * 1000)

                    binding.apply {
                        textStatus.text = data.weather[0].description
                        textWind.text = "${data.wind.speed.toString()} KM/H"
                        textLocation.text = "${data.name}\n${data.sys.country}"
                        textTemp.text = "${data.main.temp.toInt()} °C"
                        textFeelsLike.text = "Feels like: ${data.main.feels_like.toInt()} °C"
                        textMinTemp.text = "Min Temp: ${data.main.temp_min.toInt()} °C"
                        textMaxTemp.text = "Max Temp: ${data.main.temp_max.toInt()} °C"
                        textHumidity.text = "${data.main.humidity}%"
                        textPressure.text = "${data.main.pressure}hPa"
                        textUpdateTime.text = "Last Update: ${
                            SimpleDateFormat(
                                "hh:mm a",
                                Locale.ENGLISH
                            ).format(data.dt * 1000)
                        }"

                    }
                }
            }
        }
    }
}