package com.botpanda.entities;

import com.botpanda.entities.enums.TimeGranularity;
import com.google.gson.annotations.SerializedName;
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

    @SerializedName(value="time_granularity", alternate={"granularity"})
    private TimeGranularity granularity;

    public BpCandlestick(double close){
        this.close = close;
    }
}