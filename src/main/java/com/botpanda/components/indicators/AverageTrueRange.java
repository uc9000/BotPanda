package com.botpanda.components.indicators;

import java.util.ArrayList;
import java.util.Collections;

import com.botpanda.entities.BpCandlestick;

import lombok.Getter;
import lombok.Setter;

//@Slf4j
public class AverageTrueRange implements Indicator{   
    @Setter
    private ArrayList<BpCandlestick> candles;
    @Getter
    private ArrayList<Double> atrList = new ArrayList<Double>();
    @Getter
    private Double last = 0.0;
    @Setter @Getter
    private int atrLength = 14, atrMaxListSize = 5;

    @Override
    public Double calc() {
        if(candles.size() < atrLength + 2){
            last = 0.0;
            return last;
        }
        ArrayList<Double> TrVarsList = new ArrayList<Double>();
        ArrayList<Double> TrList = new ArrayList<Double>();
        for(int i = candles.size() - atrLength ; i < candles.size() ; i++){
            BpCandlestick candle = candles.get(i);
            Double prevClose = candles.get(i-1).getClose();
            TrVarsList.clear();
            TrVarsList.add(candle.getHigh() - candle.getLow());
            TrVarsList.add(candle.getHigh() - prevClose);
            TrVarsList.add(prevClose - candle.getLow());            
            TrList.add(Collections.max(TrVarsList));
            
        }
        //log.info("TrList: " + TrList.toString());
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

    @Override
    public void setValues(ArrayList<Double> values) {
        throw new UnsupportedOperationException("ATR uses BpCandlestick instead of values<double> as source");
    }
}