package com.cs.weather3.model.weeklytempforecast;

import java.io.Serializable;

public class Body implements Serializable {
    items items;

    public items getItems() {
        return items;
    }

    public void setItems(items items) {
        this.items = items;
    }
}
