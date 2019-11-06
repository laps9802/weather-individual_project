package com.cs.weather3.model.weeklytempforecast;

import java.io.Serializable;

public class items implements Serializable {
    item item;

    public item getItem() {
        return item;
    }

    public void setItem(item item) {
        this.item = item;
    }
}
