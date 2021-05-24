package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelativeStrenghtIndex implements Indicator{
    @Getter
    private ArrayList<Double> rsiList = new ArrayList<Double>();
    @Setter @Getter
    private ArrayList<Double> values = new ArrayList<Double>();
    @Setter @Getter
    private int rsiListMaxLength = 8, rsiLength = 15, lastElements = 3;
    @Getter
    private double lastRs, lastAvgLoss, lastAvgGain;
    @Setter @Getter
    private double minRsi = 25, maxRsi = 75;

    private double RS(){
        double up = 0, down = 0;
        int cnt=0;
        double prevValue = 0;
        int start = values.size() - rsiLength;
        start = (start < 0) ? 0 : start;
        for(int i = start; i < values.size(); i++){
            Double value = values.get(i);
            if (cnt == 0){
                prevValue = value;
                cnt++;
                continue;
            }
            double close = value;
            double change = close - prevValue;
            if (change < 0){
                down -= change;
            }
            else{
                up += change;
            }
            log.trace("close : " + close + " ; prevValue: " + prevValue);
            prevValue = close;
            cnt++;
        }
        lastAvgGain = up/(cnt - 1);
        lastAvgLoss = down/(cnt - 1);
        this.lastRs = lastAvgGain/lastAvgLoss;
        return this.lastRs;
    }

    public Double calc(){
        if (values.size() < rsiLength){
            return 50.0;
        }
        double result = 100 - (100 / (1 + RS()));
        rsiList.add(result);
        if(rsiList.size() > rsiListMaxLength){
            rsiList.remove(0);
        }
        return result;
    }

    @Override
    public boolean shouldBuy(){
        if(lastElements > rsiList.size()){
            lastElements = rsiList.size();
        }
        else if(lastElements < 2){
            lastElements = 2;
        }
        if(rsiList.get(rsiList.size() - lastElements) > minRsi){
            return false;
        }
        for (int i = rsiList.size() - lastElements + 1; i < rsiList.size(); i++){
            if (rsiList.get(i) < minRsi || rsiList.get(i) > maxRsi){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldSell(){
        if(rsiList.size() < 2 || rsiList.get(rsiList.size() - 2) < maxRsi){
            return false;
        }
        if (rsiList.get(rsiList.size() -1) > maxRsi){
            return false;
        }
        return true;
    }

    @Override
    public Double getLast(){
        if (rsiList.size() > 0){
            return rsiList.get(rsiList.size() - 1);
        }
        return null;       
    }
}