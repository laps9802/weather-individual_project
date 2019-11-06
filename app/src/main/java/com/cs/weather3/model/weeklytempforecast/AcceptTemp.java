package com.cs.weather3.model.weeklytempforecast;

import java.io.Serializable;

public class AcceptTemp implements Serializable {
    Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
