package com.cs.weather3.model.weather;

import java.io.Serializable;

public class HourWeather implements Serializable {
    private String baseDate;
    private String baseTime;
    private String category;
    private String fcstDate;
    private String fcstTime;
    private double fcstValue;
    private int nx;
    private int ny;



    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFcstDate() {
        return fcstDate;
    }

    public void setFcstDate(String fcstDate) {
        this.fcstDate = fcstDate;
    }

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }

    public double getFcstValue() {
        return fcstValue;
    }

    public void setFcstValue(int fcstValue) {
        this.fcstValue = fcstValue;
    }

    public int getNx() {
        return nx;
    }

    public void setNx(int nx) {
        this.nx = nx;
    }

    public int getNy() {
        return ny;
    }

    public void setNy(int ny) {
        this.ny = ny;
    }


    @Override
    public String toString() {
        return "HourWeather{" +
                "baseDate=" + baseDate +
                ", baseTime='" + baseTime + '\'' +
                ", category='" + category + '\'' +
                ", fcstDate=" + fcstDate +
                ", fcstTime=" + fcstTime +
                ", fcstValue=" + fcstValue +
                ", nx=" + nx +
                ", ny=" + ny +
                '}';
    }
}
