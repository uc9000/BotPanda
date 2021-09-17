package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MACD implements Indicator{
    @Getter
    private final ArrayList<Double> macd   = new ArrayList<>();
    @Getter
    private final ArrayList<Double> signal = new ArrayList<>();
    @Getter
    private final ArrayList<Double> histogram = new ArrayList<>();
    private ArrayList<Double> values = new ArrayList<>();
    private final ExponentialMovingAverage fastEma = new ExponentialMovingAverage();
    private final ExponentialMovingAverage slowEma = new ExponentialMovingAverage();
    private final ExponentialMovingAverage signalEma = new ExponentialMovingAverage();
    @Getter @Setter
    private int listLength = 6;
    @Getter @Setter
    private int fastLength, slowLength, signalLength;
    @Getter
    private Double last, lastSignal, lastHistogram;


    public void setParams(int fastLength, int slowLength, int signalLength){
        this.fastLength   = fastLength;
        this.slowLength   = slowLength   ;
        this.signalLength = signalLength ;
        fastEma.setEmaLength(fastLength);
        slowEma.setEmaLength(slowLength);
        signalEma.setEmaLength(signalLength);
    }

    public MACD(){
        setParams(12, 26, 9);
        signalEma.setValues(macd);
    }

    public MACD(ArrayList<Double> values){
        this();
        setValues(values);
    }

    @Override
    public Double calc() {
        last = fastEma.calc() - slowEma.calc();
        if(values.size() > slowLength){
            macd.add(last);
        }
        lastSignal = signalEma.calc();
        lastHistogram = last - lastSignal;
        if(macd.size() > signalLength){            
            signal.add(lastSignal);            
            histogram.add(lastHistogram);
        }        
        if(histogram.size() > listLength){
            signal.remove(0);
            histogram.remove(0);
        }
        if(macd.size() > signalLength + 2){
            macd.remove(0);
        }
        log.debug("histogram list = \n" + histogram);
        log.debug("signal list = \n" + signal);
        log.debug("macd list = \n" + macd);
        return last;
    }

    private boolean changedOnce(int range){
        int start = histogram.size() - range;
        start = Math.max(start, 0);
        int changed = 0;
        boolean positive = histogram.get(0) > 0;
        for(int i = start ; i < histogram.size() -1; i++){
            if(positive != histogram.get(i) > 0){
                positive = histogram.get(i) > 0;
                changed++;
            }
            if(changed > 1){
                return false;
            }
        }
        return changed == 1;
    }

    @Override
    public boolean shouldBuy() {
        return changedOnce(3)
                &&
                (lastHistogram > 0 || extrapolatedHistogram() > 0);
    }

    @Override
    public boolean shouldSell() {
        return changedOnce(3)
                &&
                (lastHistogram < 0 || extrapolatedHistogram() < 0);
    }

    @Override
    public void setValues(ArrayList<Double> values) {
        this.values = values;
        fastEma.setValues(values);
        slowEma.setValues(values);        
    }

    @Override
    public void clear() {
        fastEma.clear();
        slowEma.clear();
        macd.clear();
        signal.clear();
        histogram.clear();        
    }
}