package com.botpanda.services;

import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.Unit;

import lombok.Data;

//@Component
@Data
public class BotSettings {
    private Currency fromCurrency, toCurrency;
    private Unit unit;
    private int period, maxCandles;
    private boolean started = false;
    private double fiatPriceLimit; // Max trade amount of your default fiat currency (EUR or USD)
    private double cryptoPriceLimit; // Max trade amount of your crypto currency (BTC, ETH etc.)
    private double stopLoss; // Fraction of price you don't want to go below - 0.01 means it will sell when gain is below -1% of buying price
    private double target; // Fraction of buying price - 0.02 means is will sell when gain reaches 2%

    public BotSettings(){
        unit = Unit.MINUTES;
        period = 1;
        fromCurrency = Currency.XRP;
        toCurrency = Currency.EUR;
        maxCandles = 15;
        fiatPriceLimit = 10;
        cryptoPriceLimit = 500;
        stopLoss = 0.01;
        target = 0.02;
    }
}