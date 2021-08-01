package com.botpanda.components.indicators;

import java.util.ArrayList;
import java.util.Collections;

import com.botpanda.entities.BpCandlestick;

import lombok.Getter;
import lombok.Setter;

//@Slf4j
public class AverageTrueRange implements Indicator{    
    @Setter
    private ArrayList<Double> values;
    @Setter
    private ArrayList<BpCandlestick> candles;
    @Getter
    private ArrayList<Double> atrList = new ArrayList<Double>();
    @Getter
    private Double last = 0.0;
    @Setter @Getter
    private int atrLength, atrMaxListSize = 5;

    @Override
    public Double calc() {
        ArrayList<Double> TrVarsList = new ArrayList<>();
        ArrayList<Double> TrList = new ArrayList<>();
        for(int i = candles.size() - atrLength ; i < candles.size() ; i++){
            BpCandlestick candle = candles.get(i);
            Double prevClose = candles.get(i-1).getClose();
            TrVarsList.clear();
            TrVarsList.add(candle.getHigh() - candle.getLow());
            TrVarsList.add(candle.getHigh() - prevClose);
            TrVarsList.add(prevClose - candle.getLow());
            TrList.add(Collections.max(TrVarsList));
        }
        last = ExponentialMovingAverage.simpleAverage(TrList, atrLength);
        atrList.add(last);
        if(atrList.size() > atrMaxListSize){
            atrList.remove(0);
        }
        return last;
    }

    @Override
    public boolean shouldBuy() {
        throw new UnsupportedOperationException("Not applicable for ATR indicator");
    }

    @Override
    public boolean shouldSell() {
        throw new UnsupportedOperationException("Not applicable for ATR indicator");
    }

    @Override
    public void clear() {
        atrList.clear();        
    }
}