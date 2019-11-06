package com.cs.weather3.network;

import com.cs.weather3.AppConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRetrofitSetup {
    private WeatherRetrofitInterface weatherRetrofitInterface;

    public WeatherRetrofitSetup(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherRetrofitInterface = retrofit.create(WeatherRetrofitInterface.class);
    }

    public WeatherRetrofitInterface getRetrofitInterface(){
        return weatherRetrofitInterface;
    }
}
