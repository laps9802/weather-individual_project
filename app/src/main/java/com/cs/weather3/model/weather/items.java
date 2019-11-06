package com.cs.weather3.model.weather;

import java.io.Serializable;
import java.util.ArrayList;

public class items implements Serializable {
    private ArrayList<HourWeather> item;


    public ArrayList<HourWeather> getItem() {
        return item;
    }

    public void setItem(ArrayList<HourWeather> item) {
        this.item = item;
    }
}
