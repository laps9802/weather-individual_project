package com.cs.weather3.model.weeklylandforecast;

import java.io.Serializable;

public class Response implements Serializable {
    Body body;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}
