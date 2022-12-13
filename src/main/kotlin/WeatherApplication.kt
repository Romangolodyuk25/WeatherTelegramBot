import bot.WeatherBot
import data.remote.RetrofitClient
import data.remote.RetrofitType
import data.remote.repository.WeatherRepository

fun main(){
    //Здесь я создаю бота, Бот принимает в конструктор РЕПОЗИТОРИЙ
    val weatherRetrofitClient = RetrofitClient().getRetrofit(RetrofitType.WEATHER)
    val reverseRetrofitClient = RetrofitClient().getRetrofit(RetrofitType.REVERSE_GEOCODER)
    val weatherApi = RetrofitClient().getWeatherApi(weatherRetrofitClient)
    val reverseApi = RetrofitClient().getReversedGeocodingApi(reverseRetrofitClient)
    val weatherRepository = WeatherRepository(weatherApi, reverseApi)
    val weatherBot = WeatherBot(weatherRepository).createBot()
    weatherBot.startPolling() // метод общение клиента и сервера Poll - опрос(клиент опрашивает бота)

}