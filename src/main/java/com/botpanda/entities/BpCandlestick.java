package com.botpanda.entities;

import org.springframework.stereotype.Component;

import lombok.Data;

@SuppressWarnings("unused")
@Data
public class BpCandlestick {
    private String instrument_code;
    private double high;
    private double low;
    private double open;
    private double close;
}