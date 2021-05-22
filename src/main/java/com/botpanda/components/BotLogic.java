package com.botpanda.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
    private double cryptoBalance = 0;
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
    private final static double MAKER_FEE = 0.001 , TAKER_FEE = 0.0015;

    //Constructors:
    public BotLogic(){
        candleList = new ArrayList<BpCandlestick>(settings.getMaxCandles());
    }

    private double RS(){
        double up = 0, down = 0;
        int cnt=0;
        double prevClose = 0;
        int start = candleList.size() - settings.getRsiLength() - 1;
        start = (start < 0) ? 0 : start;
        for(int i = start; i < candleList.size(); i++){
            BpCandlestick candle = candleList.get(i);
            if (cnt == 0){
                prevClose = candle.getClose();
                cnt++;
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
            cnt++;
        }
        lastAvgGain = up/(cnt - 1);
        lastAvgLoss = down/(cnt - 1);
        this.lastRs = lastAvgGain/lastAvgLoss;
        return this.lastRs;
    }

    public double calcRSI(){
        if (candleList.size() < settings.getSafetyFactor()){
            return 50;
        }
        double result = 100 - (100 / (1 + RS()));
        return result;
    }
    
    public double RSI(){
        double result = calcRSI();
        rsiList.add(result);
        while(rsiList.size() > settings.getSafetyFactor() + 1){
            rsiList.remove(0);
        }
        log.info("RSI list:\n" + rsiList.toString());
        return result;
    }

    public boolean shouldBuy(){
        if(isCrashing() || !shouldBuy(rsiList.size())){
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
        if(rsiList.get(rsiList.size() - lastElements) > settings.getRsiMin() || bought){
            return false;
        }
        for (int i = rsiList.size() - lastElements + 1; i < rsiList.size(); i++){
            if (rsiList.get(i) < settings.getRsiMin() || rsiList.get(i) > settings.getRsiMax() - settings.getSafetyFactor()){
                return false;
            }
        }
        return true;
    }

    public boolean shouldSell(){
        if(!bought){
            return false;
        }
        if(targetReached(1) || stopLossReached(rsiList.size())){
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
        if(rsiList.get(rsiList.size() - lastElements) < settings.getRsiMax() || !bought){
            return false;
        }
        for (int i = rsiList.size() - lastElements + 1; i < rsiList.size() && i >= 0; i++){
            if (rsiList.get(i) > settings.getRsiMax()){
                return false;
            }
        }
        return true;
    }

    public void addCandle(BpCandlestick candle){
        log.info("Adding candle: " + candle.getClose());
        lastClosing = candle.getClose();
        this.candleList.add(candle);
        log.debug("List after adding candle: \n" + this.candleList.toString() + "\n Arr size: " + this.candleList.size());
        while(candleList.size() > settings.getMaxCandles() + 1){
            log.trace("Removing candle:\n" + this.candleList.get(0).toString() + "\n Arr size: " + this.candleList.size());
            this.candleList.remove(0);
        }
        if(candleList.size() >= settings.getRsiLength()){
            RSI();
        }
        if(bought){
            gainList.add(currentGain());
        }
        while (gainList.size() > settings.getSafetyFactor()){
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

    public static double gain(final double before, final double after){
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
        BigDecimal bd = new BigDecimal(amount * (1 - TAKER_FEE)).setScale(settings.getFromCurrency().getAmountPrecision(), RoundingMode.DOWN);
        boughtFor = bd.doubleValue();
        return amount;
    }

    public double amountToSell(){
        BigDecimal bd = new BigDecimal(boughtFor * (1 - MAKER_FEE)).setScale(settings.getFromCurrency().getAmountPrecision(), RoundingMode.DOWN);
        return bd.doubleValue();
    }

    public boolean targetReached(int lastElements){
        if(lastElements < 1 || gainList.size() < settings.getSafetyFactor()){
            return false;
        }
        if(lastElements >= gainList.size()){
            lastElements = gainList.size() -1;
        }
        for(int i = 0 ; i < lastElements ; i++){
            if(gainList.get(gainList.size() - 1 - i) < settings.getTarget()){
                return false;
            }
        }
        return true;
    }

    public boolean stopLossReached(int lastElements){
        if(lastElements < 1 || gainList.size() < settings.getSafetyFactor()){
            return false;
        }
        if(lastElements >= gainList.size()){
            lastElements = gainList.size() -1;
        }
        for(int i = 0 ; i < lastElements ; i++){
            if(gainList.get(gainList.size() - 1 - i) > -1 * settings.getStopLoss()){
                return false;
            }
        }
        return true;
    }

    public boolean isCrashing(){
        LinkedList <Double> first = new LinkedList<Double>();
        LinkedList <Double> last = new LinkedList<Double>();
        for(int i = 0; i < 9 && i < candleList.size(); i++){
            first.add(candleList.get(i).getClose());
            last.add(candleList.get(candleList.size() - 1 - i).getClose());
        }
        Collections.sort(first);
        Collections.sort(last);
        double firstMedian, lastMedian;
        firstMedian = first.get(first.size()/2);
        lastMedian = last.get(last.size()/2);
        double longGain = gain(firstMedian, lastMedian);
        log.debug("f median = " + firstMedian + " ; l median = " + lastMedian);
        if(longGain < (-1 * settings.getCrashIndicator())){
            log.info("Is crashing! ; long gain: " + longGain * 100 + "%");
            return true;
        }
        return false;
    }
}