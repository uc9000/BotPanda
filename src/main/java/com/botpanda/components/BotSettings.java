package com.botpanda.components;

import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.Strategy;
import com.botpanda.entities.enums.Unit;

import lombok.Data;

//@Component
@Data
public class BotSettings {
    private Currency fromCurrency, toCurrency;
    private Unit unit;
    private int period, maxCandles;
    private boolean started = false;
    private boolean testingMode = false;
    private double fiatPriceLimit; // Max trade amount of your default fiat currency (EUR or USD)
    private double cryptoPriceLimit; // Max trade amount of your crypto currency (BTC, ETH etc.)
    private double stopLoss; // Fraction of price you don't want to go below - 0.01 means it will sell when gain is below -1% of buying price
    private double atrStopLoss;
    private double safetyFactor;
    private double target; // Fraction of buying price - 0.02 means is will sell when gain reaches 2%
    private double atrTarget;
    private double rsiMin, rsiMax; //RSI thresholds
    private int rsiLength, atrLength;
    private int rsiListLength;
    private double crashIndicator;
    private Strategy strategy;
    private int emaLength;

    //default settings
    public BotSettings(){
        unit = Unit.MINUTES;
        period = 1;
        fromCurrency = Currency.XRP;
        toCurrency = Currency.EUR;
        maxCandles = 200;
        safetyFactor = 2;
        fiatPriceLimit = 25;
        cryptoPriceLimit = 500;
        stopLoss = 0.01;
        atrStopLoss = 2;
        target = 0.04;
        atrTarget = 3.5;
        rsiMin = 25;
        rsiMax = 75;
        rsiLength = 15;
        atrLength = 14;
        crashIndicator = 0.12;
        if(rsiLength >= maxCandles){
            maxCandles = rsiLength + 1;
        }
        strategy = Strategy.MACD_RSI_EMA;
        emaLength = 200;
    }
}