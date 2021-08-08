package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MACD implements Indicator{
    @Getter
    private ArrayList<Double> macd   = new ArrayList<Double>();
    @Getter
    private ArrayList<Double> signal = new ArrayList<Double>();
    @Getter
    private ArrayList<Double> histogram = new ArrayList<Double>();
    private ArrayList<Double> values = new ArrayList<Double>();
    private ExponentialMovingAverage fastEma = new ExponentialMovingAverage();
    private ExponentialMovingAverage slowEma = new ExponentialMovingAverage();
    private ExponentialMovingAverage signalEma = new ExponentialMovingAverage();
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

    public Double extrapolatedHistogram(){        
        Double change = histogram.get(listLength - 1) - histogram.get(listLength - 2);
        log.info("Avg change: " + change);
        return last + change;
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
        start = (start < 0) ? 0 : start;
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
        if(changed == 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldBuy() {
        if(changedOnce(3) 
        && 
        (lastHistogram > 0 || extrapolatedHistogram() > 0)){
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldSell() {
        if(changedOnce(3)
        && 
        (lastHistogram < 0 || extrapolatedHistogram() < 0)){
            return true;
        }
        return false;
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