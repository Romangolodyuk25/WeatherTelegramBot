package data.remote

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import data.remote.api.ReversedGeocodingApi
import data.remote.api.WeatherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val WEATHER_BASE_URL = "http://api.weatherapi.com/v1/"
const val REVERSE_GEOCODER_BASE_URL = "https://nominate.openstreetmap.org/"
const val API_KEY = "dd59ec92c86029349a140db5ef918344"

enum class RetrofitType(val baseurl: String){
    WEATHER(WEATHER_BASE_URL),
    REVERSE_GEOCODER(REVERSE_GEOCODER_BASE_URL)
}

class RetrofitClient {//удобная работа с запросами в сеть нужно создать клиент и апи


        private fun getClient(): OkHttpClient { // С ним работает рутрофит
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY // будет логировать тело
            val okHttpClient = OkHttpClient.Builder()
            okHttpClient.addInterceptor(logging)// это перехватчик ответов и запросов
            return okHttpClient.build()
        }

        fun getRetrofit(retrofitType: RetrofitType): Retrofit {
            return Retrofit.Builder()
                .baseUrl(retrofitType.baseurl)
                .client(getClient())
                .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())//расширение ретрофита функционалом, добавляет карутин
                .addConverterFactory(GsonConverterFactory.create())// выбрал в каком формате передавать данные
                .build()
        }

        fun getWeatherApi(retrofit: Retrofit): WeatherApi { // функция котора будет возвращать  API(из интерфейся WeatherApi)
            return retrofit.create(WeatherApi::class.java)
        }

        fun getReversedGeocodingApi(retrofit: Retrofit): ReversedGeocodingApi {
            return retrofit.create(ReversedGeocodingApi::class.java)
        }
}