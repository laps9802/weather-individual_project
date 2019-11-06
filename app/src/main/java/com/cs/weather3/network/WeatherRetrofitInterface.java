package com.cs.weather3.network;

import com.cs.weather3.model.midtermprospect.AcceptProspect;
import com.cs.weather3.model.weather.AcceptClass;
import com.cs.weather3.model.geocode.AcceptGeocode;
import com.cs.weather3.model.weeklylandforecast.AcceptLand;
import com.cs.weather3.model.weeklytempforecast.AcceptTemp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherRetrofitInterface {
    //***초단기 예보
    @GET("/service/SecndSrtpdFrcstInfoService2/ForecastTimeData")      //안되면 슬래쉬
    Call<AcceptClass> getHourWeather(
            @Query(value = "ServiceKey", encoded = true) String ServiceKey,     //API키에 특수문자 '%'가 있어서 인코딩 시(% -> %25) 파라미터가 제대로 전달되지 못함. encoded = true
            @Query("base_date") String base_date,
            @Query("base_time") String base_time,
            @Query("nx") int nx,
            @Query("ny") int ny,
            @Query("_type") String _type,
            @Query("pageNo") String pageNo,
            @Query("numOfRows") String numOfRows
    );

    //***동네 예보
    @GET("/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData")      //안되면 슬래쉬
    Call<AcceptClass> getThreeHourWeather(
            @Query(value = "ServiceKey", encoded = true) String ServiceKey,
            @Query("base_date") String base_date,
            @Query("base_time") String base_time,
            @Query("nx") int nx,
            @Query("ny") int ny,
            @Query("_type") String _type,
            @Query("pageNo") String pageNo,
            @Query("numOfRows") String numOfRows
    );





    //***중기 육상 예보
    @GET("/service/MiddleFrcstInfoService/getMiddleLandWeather")      //안되면 슬래쉬
    Call<AcceptLand> getWeeklyLandWeather(
            @Query(value = "ServiceKey", encoded = true) String ServiceKey,
            @Query("regId") String regId,
            @Query("tmFc") String tmFc,
            @Query("_type") String _type,
            @Query("pageNo") String pageNo,
            @Query("numOfRows") String numOfRows
    );

    //***중기 기온 예보
    @GET("/service/MiddleFrcstInfoService/getMiddleTemperature")      //안되면 슬래쉬
    Call<AcceptTemp> getWeeklyTempWeather(
            @Query(value = "ServiceKey", encoded = true) String ServiceKey,
            @Query("regId") String regId,
            @Query("tmFc") String tmFc,
            @Query("_type") String _type,
            @Query("pageNo") String pageNo,
            @Query("numOfRows") String numOfRows
    );


    //***중기 기상 전망
    @GET("/service/MiddleFrcstInfoService/getMiddleForecast")      //안되면 슬래쉬
    Call<AcceptProspect> getMidTermProspectWeather(
            @Query(value = "ServiceKey", encoded = true) String ServiceKey,
            @Query("stnId") String stnId,
            @Query("tmFc") String tmFc,
            @Query("_type") String _type,
            @Query("pageNo") String pageNo,
            @Query("numOfRows") String numOfRows
    );
}
