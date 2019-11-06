package com.cs.weather3.model.weather;

import java.io.Serializable;

public class Body implements Serializable {
    private items items;


    public items getItems() {
        return items;
    }

    public void setItems(items items) {
        this.items = items;
    }
}
