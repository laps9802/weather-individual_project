package com.cs.weather3.network;

import com.cs.weather3.AppConstants;
import com.cs.weather3.model.geocode.AcceptGeocode;
import com.cs.weather3.model.midtermprospect.AcceptProspect;
import com.cs.weather3.model.weather.AcceptClass;
import com.cs.weather3.model.weeklylandforecast.AcceptLand;
import com.cs.weather3.model.weeklytempforecast.AcceptTemp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface NaverMapRetrofitInterface {
    @Headers({"X-NCP-APIGW-API-KEY-ID: " + AppConstants.NAVER_MAP_API_ID, "X-NCP-APIGW-API-KEY: " + AppConstants.NAVER_MAP_API_KEY})

    //***네이버 API 위경도 -> 주소 가져오기
    @GET("/map-reversegeocode/v2/gc")      //안되면 슬래쉬
    Call<AcceptGeocode> getReverseGeocode(
            @Query("request") String request,       //필수
            @Query("coords") String coords,         //위경도
            @Query("orders") String orders,         //주소 형태 (지번 주소)
            @Query("output") String output          //출력 형태 (json)
    );
}
