package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExponentialMovingAverage implements Indicator{
    @Getter
    private final ArrayList<Double> emaList = new ArrayList<>();
    @Getter
    private Double last = 0.0;
    @Setter
    private ArrayList<Double> values;
    @Setter @Getter
    private int emaLength = 50, maxEmaListLength = 3;

    private Double currentEma(Double close, Double previous){        
        double multiplier = (2.0 / (emaLength + 1));
        return multiplier * (close - previous) + previous;
    }

    private double lastValue(){
        return values.get(values.size()-1);
    }

    public static Double simpleAverage(ArrayList<Double> values, int firstElements){
        if(values.size() < 1){
            return 0.0;
        }
        Double sum = 0.0;
        int i;
        for(i = 0; i < firstElements && i < values.size(); i++){
            sum += values.get(i);
        }
        return sum/i;
    }

    private Double simpleAverage(){
        return simpleAverage(values, emaLength);
    }

    @Override
    public Double calc(){
        if(values.size() < emaLength || emaList.size() == 0){
            last = simpleAverage();
            emaList.add(last);
            return last;
        }
        emaList.add(currentEma(lastValue(), last));
        last = emaList.get(emaList.size() - 1);
        if(emaList.size() > maxEmaListLength){
            emaList.remove(0);
        }
        log.trace("EMA list =\n" + emaList);        
        return last;
    }

    @Override
    public boolean shouldBuy() {
        if(lastValue() > last){
            log.debug("BUY signal EMA");
            return true;
        }
        return false;
    }
    @Override
    public boolean shouldSell() {
        if(lastValue() > last){
            return false;
        }
        log.debug("SELL signal EMA");
        return true;
    }

    @Override
    public void clear() {
        emaList.clear();        
    }
}