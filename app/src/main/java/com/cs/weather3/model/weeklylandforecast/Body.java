package com.cs.weather3.model.weeklylandforecast;

import java.io.Serializable;

public class Body implements Serializable {
    items items;

    public com.cs.weather3.model.weeklylandforecast.items getItems() {
        return items;
    }

    public void setItems(com.cs.weather3.model.weeklylandforecast.items items) {
        this.items = items;
    }
}
