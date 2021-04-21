package com.botpanda.models;

import org.springframework.stereotype.Component;

import lombok.Data;

@SuppressWarnings("unused")
@Data
@Component
public class BpCandlestick {
    private double high;
    private double low;
    private double close;
}
