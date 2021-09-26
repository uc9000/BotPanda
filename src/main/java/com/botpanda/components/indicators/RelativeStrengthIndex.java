package com.botpanda.components.indicators;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelativeStrengthIndex implements Indicator{
    @Getter
    private final ArrayList<Double> rsiList = new ArrayList<>();
    @Setter
    private ArrayList<Double> values = new ArrayList<>();
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
        start = Math.max(start, 0);
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
                cnt++;
            }
            else if(change > 0){
                up += change;
                cnt++;
            }
            log.trace("close : " + close + " ; prevValue: " + prevValue);
            prevValue = close;
        }
        if(cnt > 2){
            lastAvgGain = up/cnt;
            lastAvgLoss = down/cnt;
            this.lastRs = up/down;
        }
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
        log.debug("RSI list =\n" + rsiList);
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
        //log.debug("BUY signal RSI");
        return true;
    }

    @Override
    public boolean shouldSell(){
        if(rsiList.size() < 2 || rsiList.get(rsiList.size() - 2) < maxRsi){
            return false;
        }
        return !(rsiList.get(rsiList.size() - 1) > maxRsi);
        //log.debug("SELL signal RSI");
    }

    @Override
    public Double getLast(){
        if (rsiList.size() > 0){
            return rsiList.get(rsiList.size() - 1);
        }
        return null;       
    }

    @Override
    public void clear() {
        rsiList.clear();        
    }
}