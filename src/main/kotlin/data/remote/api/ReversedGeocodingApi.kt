package data.remote.api

import data.remote.models.ReversedCountry
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface ReversedGeocodingApi { // описываем повидение(функцию которая будет совершать запросы)

    @GET("reverse")// без слэша потому что он стоит в base url
    fun getCountryNameByCoordinates (
        @Query("lat") latitude: String,// указал параметр(есть обязательные и нет) названии согласно документации
        @Query("lon") longitude: String,
        @Query ("format") formatData: String// передавать в него будем json
    ) : Deferred<ReversedCountry>
}