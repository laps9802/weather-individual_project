package com.cs.weather3.model.geocode;

import java.io.Serializable;
import java.util.ArrayList;


public class AcceptGeocode implements Serializable {
    ArrayList<Orders> results;

    public ArrayList<Orders> getResults() {
        return results;
    }

    public void setResults(ArrayList<Orders> results) {
        this.results = results;
    }
}
