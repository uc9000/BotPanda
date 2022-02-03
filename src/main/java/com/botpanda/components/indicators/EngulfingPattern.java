package com.botpanda.components.indicators;

import com.botpanda.entities.BpCandlestick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EngulfingPattern implements Indicator{
    private final ArrayList<BpCandlestick> candleList;
    private int engulfing = 0;

    public EngulfingPattern(){
        throw new IllegalStateException("!!!candle list must be passed!!!");
    }

    public EngulfingPattern(ArrayList<BpCandlestick> candleList){
        this.candleList = candleList;
    }

    private List<Double> candleToList(BpCandlestick candle){
        List<Double> list = new ArrayList<>();
        list.add(candle.getLow());
        list.add(candle.getClose());
        list.add(candle.getOpen());
        list.add(candle.getHigh());
        Collections.sort(list);
        return list;
    }

    private boolean engulfingSide(BpCandlestick first, BpCandlestick second){
        List<Double> firstSet = candleToList(first),
                secondSet = candleToList(second);

        return secondSet.get(2) >= firstSet.get(2) && secondSet.get(3) >= firstSet.get(3)
                && secondSet.get(0) <= firstSet.get(0) && secondSet.get(1) <= firstSet.get(1);
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