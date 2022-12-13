package data.remote.repository

import data.remote.api.ReversedGeocodingApi
import data.remote.api.WeatherApi
import data.remote.models.CurrentWeather
import data.remote.models.ReversedCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//репозиторий работает с данными(БАЗА ДАННЫХ) решит какие и откуда данные мне брать
class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val reversedGeocodingApi: ReversedGeocodingApi
) {

    // назвал так же как и в Его AПИ
   suspend fun getCurrentWeather(apiKey: String, countryName: String, airQualityData: String): CurrentWeather {
        return withContext(Dispatchers.IO) { //Для переключениями между потоками
            weatherApi.getCurrentWeatherAsync(apiKey, countryName, airQualityData)
        }.await() // почитать про корутины
    }

    suspend fun getReversedGeocodingCountryName(latitude: String, longitude: String, format: String): ReversedCountry {
        return withContext(Dispatchers.IO) {//Для переключениями между потоками
            reversedGeocodingApi.getCountryNameByCoordinates(latitude,longitude,format)
        }.await()
    }
}