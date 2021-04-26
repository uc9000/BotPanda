package com.botpanda.services;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Service
public class BotSettings {
    @Setter
    @Getter
    private String fromCurrency, toCurrency, unit;
    @Setter
    @Getter
    private int period, maxCandles;
    @Getter
    private boolean started = false;

    public BotSettings(){
        unit = "MINUTES";
        period = 1;
        fromCurrency = "BTC";
        toCurrency = "EUR";
        maxCandles = 15;
    }
}