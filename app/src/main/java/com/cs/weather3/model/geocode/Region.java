package com.cs.weather3.model.geocode;

import java.io.Serializable;


public class Region implements Serializable{
    Area1 area1;
    Area2 area2;

    public Area2 getArea2() {
        return area2;
    }

    public void setArea2(Area2 area2) {
        this.area2 = area2;
    }

    public Area1 getArea1() {
        return area1;
    }

    public void setArea1(Area1 area1) {
        this.area1 = area1;
    }
}


