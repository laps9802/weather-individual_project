package com.cs.weather3.model.weeklylandforecast;

import java.io.Serializable;

public class AcceptLand implements Serializable {
    Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
