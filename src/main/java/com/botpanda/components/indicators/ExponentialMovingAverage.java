package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExponentialMovingAverage implements Indicator{
    @Getter
    private ArrayList<Double> emaList = new ArrayList<Double>();
    @Getter
    private Double last;
    @Setter
    private ArrayList<Double> values;
    @Setter @Getter
    private int emaLength = 100, maxEmaListLength = 8;

    private Double currentEma(Double close, Double previous){
        if(values.size() < 2){
            return simpleAverage();
        }
        Double multiplier = (2.0 / (emaLength + 1));
        return multiplier * (close - previous) + previous;
    }

    private Double simpleAverage(){
        Double sum = 0.0;
        int i;
        for(i = 0; i < emaLength && i < values.size(); i++){
            sum += values.get(i);
        }
        return sum/i;
    }

    @Override
    public Double calc(){ 
        log.debug("ema calc()");       
        if(emaList.size() == 0){
            last = simpleAverage();
            emaList.add(last);
        }
        emaList.add(currentEma(values.get(values.size() - 1), last));
        last = emaList.get(emaList.size() - 1);            
        if(emaList.size() > maxEmaListLength){
            emaList.remove(0);
        }
        log.info("EMA list =\n" + emaList);        
        return last;
    }

    @Override
    public boolean shouldBuy() {
        if(values.get(values.size() - 1) > last){
            return true;
        }
        return false;
    }
    @Override
    public boolean shouldSell() {
        if(values.get(values.size() - 1) > last){
            return false;
        }
        return true;
    }
}