package com.cs.weather3.model.weather;

import java.io.Serializable;

public class AcceptClass implements Serializable {
    Response response;



    public Response getResponse() {
        return response;
    }
    public void setResponse(Response response) {
        this.response = response;
    }
}


