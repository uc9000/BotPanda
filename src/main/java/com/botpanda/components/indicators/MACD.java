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
    ExponentialMovingAverage fastEma = new ExponentialMovingAverage();
    ExponentialMovingAverage slowEma = new ExponentialMovingAverage();
    ExponentialMovingAverage signalEma = new ExponentialMovingAverage();
    @Getter @Setter
    private int listLength = 6;
    @Getter
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

    @Override
    public boolean shouldBuy() {
        if(lastHistogram > 0 && histogram.get(histogram.size() - 2) < 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldSell() {
        if(lastHistogram < 0 && histogram.get(histogram.size() - 2) > 0){
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
        macd.clear();
        signal.clear();
        histogram.clear();        
    }
}