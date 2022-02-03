package com.botpanda.components.indicators;

import com.botpanda.entities.BpCandlestick;

import java.util.ArrayList;

public class EngulfingPattern implements Indicator{
    private final ArrayList<BpCandlestick> candleList;
    private int engulfing = 0;

    EngulfingPattern(){
        throw new IllegalStateException("!!!candle list must be passed!!!");
    }

    EngulfingPattern(ArrayList<BpCandlestick> candleList){
        this.candleList = candleList;
    }

    private boolean engulfingSide(BpCandlestick first, BpCandlestick second){
        return second.getClose() > first.getClose() && second.getHigh() > first.getHigh()
                && second.getOpen() < first.getOpen() && second.getLow() < first.getLow()
                && first.getVolume() > second.getVolume();
    }

    @Override
    public Double calc() {
        if(candleList.size() < 2){
            return 0.0;
        }
        BpCandlestick first = candleList.get(candleList.size() - 2);
        BpCandlestick second = candleList.get(candleList.size() - 1);
        if(engulfingSide(first, second)){
            engulfing = 1;
        }else if(engulfingSide(second, first)){
            engulfing = -1;
        }else{
            engulfing = 0;
        }
        return (double)engulfing;
    }

    @Override
    public boolean shouldBuy() {
        return engulfing == 1;
    }

    @Override
    public boolean shouldSell() {
        return engulfing == -1;
    }

    @Override
    public void setValues(ArrayList<Double> values) {
        throw new IllegalStateException("Engulfing needs whole candles, not only closing values");
    }

    @Override
    public Double getLast() {
        return (double)engulfing;
    }

    @Override
    public void clear() {
        engulfing = 0;
    }
}