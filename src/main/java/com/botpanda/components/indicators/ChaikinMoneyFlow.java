package com.botpanda.components.indicators;

import java.util.ArrayList;

import com.botpanda.entities.BpCandlestick;

import lombok.Getter;
import lombok.Setter;

public class ChaikinMoneyFlow implements Indicator {
    @Setter
    private ArrayList<BpCandlestick> candles;
    @Getter
    private ArrayList<Double> cmfList = new ArrayList<Double>();
    @Getter
    private Double last = 0.0;
    @Setter @Getter
    private int cmfLength = 20, atrMaxListSize = 5;

    @Override
    public Double calc() {
        if(candles.size() < cmfLength + 1){
            last = 0.0;
            return last;
        }
        double mfVolumeSum = 0.0;
        double VolumeSum = 0.0;
        for(int i = candles.size() - 1; i >= candles.size() - cmfLength ; i--){
            BpCandlestick c = candles.get(i);
            if(c.getHigh() == c.getLow()){
                continue;
            }
            double mfMultiplier =  ((c.getClose() -c.getLow()) - (c.getHigh() - c.getClose())) / (c.getHigh() - c.getLow());
            mfVolumeSum += c.getVolume() * mfMultiplier;
            VolumeSum += c.getVolume();
        }
        last = mfVolumeSum / VolumeSum;
        cmfList.add(last);
        if(cmfList.size() > atrMaxListSize){
            cmfList.remove(0);
        }
        return last;
    }

    @Override
    public boolean shouldBuy() {
        return last > 0.0;
    }

    @Override
    public boolean shouldSell() {
        return last < 0.0;
    }

    @Override
    public void clear() {
        cmfList.clear();        
    }

    @Override
    public void setValues(ArrayList<Double> values) {
        throw new UnsupportedOperationException("CMF uses BpCandlestick instead of values<double> as source");        
    }    
}