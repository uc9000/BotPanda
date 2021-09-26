package com.botpanda.entities;

import lombok.Data;

import java.util.Date;


@Data
public class BpCandlestick {
    private String instrument_code;
    private double high;
    private double low;
    private double open;
    private double close;
    private double total_amount;
    private double volume;
    private Date time;

    public BpCandlestick(){
        close = 1;
        low = 1;
        high = 1;
        open = 1;
        instrument_code = "";
    }

    public BpCandlestick(double close){
        this();
        this.close = close;
    }
}