package com.botpanda.services;

import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.Unit;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class BotSettings {
    private Currency fromCurrency, toCurrency;
    private Unit unit;
    private int period, maxCandles;
    private boolean started = false;
    private double priceLimit; // In your default fiat currency (EUR or USD)


    public BotSettings(){
        unit = Unit.MINUTES;
        period = 1;
        fromCurrency = Currency.BTC;
        toCurrency = Currency.EUR;
        maxCandles = 15;
        priceLimit = 20;
    }
}