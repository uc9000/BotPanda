package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class ExponentialMovingAverage implements Indicator{
    @Getter
    private ArrayList<Double> emaList = new ArrayList<Double>();
    @Setter @Getter
    private ArrayList<Double> values = new ArrayList<Double>();

    @Override
    public Double calc(){
        return values.get(0);
    }

    @Override
    public boolean shouldBuy() {
        if(values.get(values.size() - 1) > emaList.get(emaList.size() - 1)){
            return true;
        }
        return false;
    }
    @Override
    public boolean shouldSell() {
        if(values.get(values.size() - 1) > emaList.get(emaList.size() - 1)){
            return false;
        }
        return true;
    }

    @Override
    public Double getLast() {
        if(emaList.size() > 0){
            return emaList.get(emaList.size() - 1);
        }
        return null;
    }   
    
}
