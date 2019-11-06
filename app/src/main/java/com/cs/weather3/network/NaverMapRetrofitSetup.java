package com.cs.weather3.network;

import com.cs.weather3.AppConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverMapRetrofitSetup {
    private NaverMapRetrofitInterface naverMapRetrofitInterface;

    public NaverMapRetrofitSetup(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.Naver_Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        naverMapRetrofitInterface = retrofit.create(NaverMapRetrofitInterface.class);
    }

    public NaverMapRetrofitInterface getRetrofitInterface(){
        return naverMapRetrofitInterface;
    }
}
