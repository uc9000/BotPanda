package com.botpanda.services;

import java.util.ArrayList;
import java.util.List;

import com.botpanda.entities.BpCandlestick;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

// @Component
// @Scope(value = "prototype")
@Slf4j
public class BotLogic {
    @Setter @Getter
    private double minRsi, maxRsi, safetyFactor;
    @Getter
    private double lastRs, lastAvgLoss, lastAvgGain;
    @Setter @Getter
    private boolean bought = false;
    @Getter
    private List <BpCandlestick> candleList;
    @Getter
    private List <Double> rsiList = new ArrayList<Double>();
    @Setter
    BotSettings settings = new BotSettings();

    //Constructors:
    public BotLogic(){
        minRsi = 30;
        maxRsi = 70;
        safetyFactor = 5;
        candleList = new ArrayList<BpCandlestick>();
    }

    public BotLogic(double min, double max){
        minRsi = min;
        maxRsi = max;
        safetyFactor = 5;
        candleList = new ArrayList<BpCandlestick>();
    }

    public BotLogic(double min, double max, double safetyFactor){
        minRsi = min;
        maxRsi = max;
        this.safetyFactor = safetyFactor;
        candleList = new ArrayList<BpCandlestick>();
    }

    private double RS(){
        double up = 0, down = 0;
        int i=0;
        double prevClose = 0;
        for(BpCandlestick candle : candleList){
            if (i == 0){
                prevClose = candle.getClose();
                i++;
                continue;
            }
            double close = candle.getClose();
            double change = close - prevClose;
            if (change < 0){
                down -= change;
            }
            else{
                up += change;
            }
            log.trace("close : " + close + " ; prevClose: " + prevClose);
            prevClose = close;
            i++;
        }
        lastAvgGain = up/(i - 1);
        lastAvgLoss = down/(i - 1);
        this.lastRs = lastAvgGain/lastAvgLoss;
        return this.lastRs;
    }

    public double RSI(){
        if (candleList.size() < safetyFactor){
            return 50;
        }
        double result = 100 - (100 / (1 + RS()));
        rsiList.add(result);
        while(rsiList.size() > safetyFactor + 1){
            rsiList.remove(0);
        }
        log.info("RSI list:\n" + rsiList.toString());
        return result;
    }
       

    public boolean shouldBuy(){
        if(rsiList.get(0) > minRsi || bought){
            return false;
        }
        for (int i = 1; i < rsiList.size(); i++){
            if (rsiList.get(i) < minRsi){
                return false;
            }
        }
        return true;
    }       

    public boolean shouldSell(){
        if(rsiList.get(0) < maxRsi || !bought){
            return false;
        }
        for (int i = 1; i < rsiList.size(); i++){
            if (rsiList.get(i) > maxRsi){
                return false;
            }
        }
        return true;
    }

    public void addCandle(BpCandlestick candle){
        //log.info("Adding candle:\n" + candle.toString());
        this.candleList.add(candle);
        log.debug("List after adding candle: \n" + this.candleList.toString() + "\n Arr size: " + this.candleList.size());
        while(candleList.size() > settings.getMaxCandles() + 1){
            log.trace("Removing candle:\n" + this.candleList.get(0).toString() + "\n Arr size: " + this.candleList.size());
            this.candleList.remove(0);
        }
        if(candleList.size() >= 4){
            RSI();
        }
    }

    public void setCandleList(List <BpCandlestick> _candleList){
        for(int i = 0; i < _candleList.size() ; i++){
            this.addCandle(_candleList.get(i));
        }
        log.debug("Set list:\n" + candleList.toString());
    }

    public BpCandlestick getLastCandle(){
        if (candleList.size() < 1){
            log.debug("List is empty!");
            return new BpCandlestick();
        }
        return candleList.get(candleList.size()-1);
    }

    public double getLastRsi(){
        if (rsiList.size() < 1){
            log.debug("List is empty!");
            return 50;
        }
        return rsiList.get(rsiList.size()-1);
    }
}