package com.cs.weather3.model.geocode;

import java.io.Serializable;


public class Orders implements Serializable{
    String name;
    Region region;




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}


