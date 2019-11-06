package com.cs.weather3.model.midtermprospect;

import java.io.Serializable;

public class AcceptProspect implements Serializable {
    Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
