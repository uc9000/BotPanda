package com.botpanda.services;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class BotSettings {
    @Setter
    @Getter
    private String fromCurrency, toCurrency, unit;
    @Setter
    @Getter
    private int period, maxCandles;
    @Getter
    @Setter
    private boolean started = false;

    public BotSettings(){
        unit = "MINUTES";
        period = 1;
        fromCurrency = "BTC";
        toCurrency = "EUR";
        maxCandles = 15;
    }
}