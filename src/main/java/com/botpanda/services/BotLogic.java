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
    private double lastRs, lastAvgLoss, lastAvgGain, lastClosing, buyingPrice = 0, sellingPrice = 0, boughtFor = 0;
    @Setter @Getter
    private boolean bought = false;
    @Getter
    private List <BpCandlestick> candleList;
    @Getter
    private List <Double> rsiList = new ArrayList<Double>();
    @Getter
    private List <Double> gainList = new ArrayList<Double>();
    @Setter
    BotSettings settings = new BotSettings();
    @Getter
    double MAKER_FEE = 0.001 , TAKER_FEE = 0.0015;

    //Constructors:
    public BotLogic(){
        minRsi = 30;
        maxRsi = 70;
        safetyFactor = 5;
        candleList = new ArrayList<BpCandlestick>();
    }

    public BotLogic(double min, double max){
        this();
        minRsi = min;
        maxRsi = max;
    }

    public BotLogic(double min, double max, double safetyFactor){
        this(min, max);
        this.safetyFactor = safetyFactor;
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
        for (int i = 1; i < rsiList.size(); i++){
            if (shouldSell(i)){
                return false;
            }
        }
        if(!shouldBuy(rsiList.size())){
            return false;
        }
        buyingPrice = lastClosing;
        return true;
    }

    public boolean shouldBuy(int lastElements){
        if(bought){
            return false;
        }
        if(lastElements > rsiList.size()){
            lastElements = rsiList.size();
        }
        else if(lastElements < 2){
            lastElements = 2;
        }
        if(rsiList.get(rsiList.size() - lastElements) > minRsi || bought){
            return false;
        }
        for (int i = rsiList.size() - lastElements + 1; i < rsiList.size(); i++){
            if (rsiList.get(i) < minRsi){
                return false;
            }
        }
        return true;
    }

    public boolean shouldSell(){
        if(!bought){
            return false;
        }
        if(targetReached(1) || stopLossReached(2)){
            return true;
        }
        for (int i = 1; i < rsiList.size(); i++){
            if (shouldBuy(i)){
                return false;
            }
        }
        if(!shouldSell(2)){
            return false;
        }
        sellingPrice = lastClosing;
        return true;
    }

    public boolean shouldSell(int lastElements){
        if(lastElements > rsiList.size()){
            lastElements = rsiList.size();
        }else if(lastElements < 2){
            lastElements = 2;
        }
        if(rsiList.get(rsiList.size() - lastElements) < maxRsi || !bought){
            return false;
        }
        for (int i = rsiList.size() - lastElements + 1; i < rsiList.size() && i >= 0; i++){
            if (rsiList.get(i) > maxRsi){
                return false;
            }
        }
        return true;
    }

    public void addCandle(BpCandlestick candle){
        log.info("Adding candle:\n" + candle.getClose());
        lastClosing = candle.getClose();
        this.candleList.add(candle);
        log.debug("List after adding candle: \n" + this.candleList.toString() + "\n Arr size: " + this.candleList.size());
        while(candleList.size() > settings.getMaxCandles() + 1){
            log.trace("Removing candle:\n" + this.candleList.get(0).toString() + "\n Arr size: " + this.candleList.size());
            this.candleList.remove(0);
        }
        if(candleList.size() > safetyFactor){
            RSI();
        }
        if(bought){
            gainList.add(currentGain());
        }
        while (gainList.size() > safetyFactor){
            gainList.remove(0);
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

    public double gain(double before, double after){
        return (((1 - TAKER_FEE) * after) - ((1 + MAKER_FEE) * before))/before;
    }

    public double currentGain(){
        return gain(this.buyingPrice, this.lastClosing);
    }

    public double amountToBuy(){
        double amount = settings.getFiatPriceLimit() / lastClosing;         
        if (amount > settings.getCryptoPriceLimit()){
            amount = settings.getCryptoPriceLimit();
        }
        boughtFor = amount;
        return amount;
    }

    public boolean targetReached(int lastElements){
        for(int i = 0 ; i < lastElements ; i++){
            if(gainList.get(gainList.size() - 1 - i) < settings.getTarget()){
                return false;
            }
        }
        return true;
    }

    public boolean stopLossReached(int lastElements){
        for(int i = 0 ; i < lastElements ; i++){
            if(gainList.get(gainList.size() - 1 - i) > -1 * settings.getStopLoss()){
                return false;
            }
        }
        return true;
    }
}