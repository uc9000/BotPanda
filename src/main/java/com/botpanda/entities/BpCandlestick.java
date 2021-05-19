package com.botpanda.entities;

import lombok.Data;


@Data
public class BpCandlestick {
    private String instrument_code;
    private double high;
    private double low;
    private double open;
    private double close;

    public BpCandlestick(){
        close = 1;
        low = 1;
        high = 1;
        open = 1;
        instrument_code = new String();
    }

    public BpCandlestick(double close){
        this();
        this.close = close;
    }
}