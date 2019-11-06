package com.cs.weather3.model.weeklylandforecast;

import java.io.Serializable;

public class items implements Serializable {
    item item;

    public com.cs.weather3.model.weeklylandforecast.item getItem() {
        return item;
    }

    public void setItem(com.cs.weather3.model.weeklylandforecast.item item) {
        this.item = item;
    }
}
