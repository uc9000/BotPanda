package com.botpanda.entities;

import com.botpanda.entities.enums.TimeGranularity;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;


@Data
public class BpCandlestick {
    private String instrument_code = "";
    private double high = 0;
    private double low = 0;
    private double open = 0;
    private double close = 0;
    private double total_amount = 0;
    private double volume = 0;
    private Date time = new Date();

    @SerializedName(value="time_granularity", alternate={"granularity"})
    private TimeGranularity granularity = TimeGranularity.MINUTES5;

    public BpCandlestick(){}

    public BpCandlestick(double close){
        this.close = close;
    }
}