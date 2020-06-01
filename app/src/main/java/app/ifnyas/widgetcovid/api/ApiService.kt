package app.ifnyas.widgetcovid.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/rawan")
    fun getRawan(
        @Query("loc", encoded = true) loc: String
    ): Call<ResponseBody>

//    @FormUrlEncoded
//    @POST("api/v1/service")
//    fun getDriver(
//        @Header("Authorization") auth: String,
//        @Field("service") service: String,
//        @Field("payload[farm_field_id]") id: String
//    ): Call<ResponseBody>
}