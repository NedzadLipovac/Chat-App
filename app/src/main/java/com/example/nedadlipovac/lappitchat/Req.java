package com.example.nedadlipovac.lappitchat;

public class Req {
    public String request_type;

    public Req() {
    }

    public Req(String request_type) {

        this.request_type = request_type;
    }

    public String getRequest_type() {

        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
