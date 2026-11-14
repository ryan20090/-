package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class AladhanResponse(
    @Json(name = "code") val code: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "data") val data: AladhanCalendarData?
)

data class AladhanCalendarData(
    @Json(name = "hijri") val hijri: AladhanHijriDate?,
    @Json(name = "gregorian") val gregorian: AladhanGregorianDate?
)

data class AladhanHijriDate(
    @Json(name = "date") val date: String?,
    @Json(name = "day") val day: String?,
    @Json(name = "month") val month: AladhanHijriMonth?,
    @Json(name = "year") val year: String?,
    @Json(name = "weekday") val weekday: AladhanWeekday?
)

data class AladhanHijriMonth(
    @Json(name = "number") val number: Int?,
    @Json(name = "en") val en: String?,
    @Json(name = "ar") val ar: String?
)

data class AladhanWeekday(
    @Json(name = "en") val en: String?,
    @Json(name = "ar") val ar: String?
)

data class AladhanGregorianDate(
    @Json(name = "date") val date: String?,
    @Json(name = "day") val day: String?,
    @Json(name = "year") val year: String?
)

interface UmmAlQuraCalendarApi {
    @GET("v1/gToH")
    suspend fun convertGregorianToHijri(
        @Query("date") dateDdMmYyyy: String
    ): AladhanResponse
}

object TrustedPlatformsNetwork {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    val ummAlQuraApi: UmmAlQuraCalendarApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UmmAlQuraCalendarApi::class.java)
    }
}
