package com.botpanda.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.botpanda.components.indicators.AverageTrueRange;
import com.botpanda.components.indicators.ChaikinMoneyFlow;
import com.botpanda.components.indicators.ExponentialMovingAverage;
import com.botpanda.components.indicators.MACD;
import com.botpanda.components.indicators.RelativeStrenghtIndex;
import com.botpanda.entities.BpCandlestick;
import com.botpanda.entities.enums.Strategy;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

// @Component
// @Scope(value = "prototype")
@Slf4j
public class BotLogic {
    @Getter
    private double lastClosing, buyingPrice = 0, sellingPrice = 0, boughtFor = 0;
    @Setter @Getter
    private boolean bought = false;
    @Getter
    private final ArrayList <BpCandlestick> candleList  = new ArrayList<>();
    private final ArrayList <Double> values = new ArrayList<>(); //closing prices
    @Getter
    private final ArrayList <Double> gainList = new ArrayList<>();
    BotSettings settings;
    @Getter
    private final static double MAKER_FEE = 0.001 , TAKER_FEE = 0.0015;

    private final RiskManagement riskManagement;

    public RelativeStrenghtIndex rsi = new RelativeStrenghtIndex();
    public ExponentialMovingAverage ema = new ExponentialMovingAverage();
    public MACD macd = new MACD();
    public AverageTrueRange atr = new AverageTrueRange();
    public ChaikinMoneyFlow cmf = new ChaikinMoneyFlow();

    //Constructors:
    public BotLogic(){
        rsi.setValues(values);
        ema.setValues(values);
        macd.setValues(values);
        atr.setCandles(candleList);
        riskManagement = new RiskManagement(settings, atr, values);
        cmf.setCandles(candleList);
    }

    public BotLogic(BotSettings settings){
        this();
        setSettings(settings);
    }
    
    public void setSettings(BotSettings settings){
        this.settings = settings;
        rsi.setLastElements((int)settings.getSafetyFactor());
        rsi.setRsiLength(settings.getRsiLength());
        ema.setEmaLength(settings.getEmaLength());
        atr.setAtrLength(settings.getAtrLength());
    }

    public boolean shouldBuy(){        
        if(settings.getStrategy().equals(Strategy.RSI_AND_EMA) && !rsi.shouldBuy())
            { return false; }
        if(settings.getStrategy().isUsingEma() && !ema.shouldBuy())  { return false; }
        if(settings.getStrategy().isUsingCmf() && !cmf.shouldBuy())  { return false; }
        if(settings.getStrategy().isUsingMacd() && !macd.shouldBuy()){ return false; }
        else if(settings.getStrategy().equals(Strategy.MACD_RSI_EMA) && rsi.getLast() < 50.0)
            { return false; }
        
        if (bought){
            return false;
        }
        riskManagement.setEntryPrice(lastClosing);
        buyingPrice = lastClosing;
        return true;
    }

    public boolean shouldSell(){
        if(!bought){ return false; }
        if(riskManagement.targetReached() || riskManagement.stopLossReached()){
            sellingPrice = lastClosing;
            return true;
        }
        if(settings.getStrategy().equals(Strategy.RSI_AND_EMA) && !rsi.shouldSell())
            { return false; }
        if(settings.getStrategy().isUsingEma()  &&  !ema.shouldSell()) { return false; }
        if(settings.getStrategy().isUsingCmf()  &&  !cmf.shouldSell()) { return false; }
        if(settings.getStrategy().isUsingMacd() && !macd.shouldSell()) { return false; }
        else if(settings.getStrategy().equals(Strategy.MACD_RSI_EMA) && rsi.getLast() >= 50){
            return false;
        }
        riskManagement.setEntryPrice(0.0);
        sellingPrice = lastClosing;
        return true;
    }

    public void addCandle(BpCandlestick candle){
        StringBuilder strategyLogMsg = new StringBuilder();
        // + " Vo: " + String.format("%.1f", candle.getVolume())
        // + " Hi: " + candle.getHigh()
        // + " Lo: " + candle.getLow()
        strategyLogMsg.append("CL: ").append(String.format("%.5f", candle.getClose()), 0, 7);
        lastClosing = candle.getClose();
        this.values.add(lastClosing);
        this.candleList.add(candle);
        log.debug("List after adding candle: \n" + this.candleList + "\n Arr size: " + this.candleList.size());
        if(candleList.size() > settings.getMaxCandles() + 1){
            log.trace("Removing candle:\n" + this.candleList.get(0).toString() + "\n Arr size: " + this.candleList.size());
            this.candleList.remove(0);
            this.values.remove(0);
        }
        if(settings.getStrategy().isUsingRsi() && values.size() > rsi.getRsiLength()){
            rsi.calc();
            strategyLogMsg.append(" RSI =").append(String.format("%.2f", rsi.getLast()));
        }
        if(settings.getStrategy().isUsingEma()){
            ema.calc();
            strategyLogMsg.append(" EMA ").append(ema.getEmaLength()).append(" =").append(ema.getLast().toString(), 0, 5);
        }
        if(settings.getAtrTarget() != 0.0 || settings.getAtrStopLoss() != 0){
            atr.calc();
            strategyLogMsg.append(" ATR = ").append(String.format("%.5f", atr.getLast()));
        }
        if(settings.getStrategy().isUsingCmf() && values.size() > cmf.getCmfLength()){
            cmf.calc();
            strategyLogMsg.append(" CMF =").append(String.format("%.4f", cmf.getLast()), 0, 5);
        }
        if(settings.getStrategy().isUsingMacd()){
            macd.calc();
            strategyLogMsg.append(" MACD Histo =").append(String.format("%.5f", macd.getLastHistogram()));
            strategyLogMsg.append(" Extr Histo =").append(String.format("%.5f", macd.extrapolatedHistogram()));
        }
        log.info(strategyLogMsg.toString());
        if(bought){
            gainList.add(currentGain());
        }
        if (gainList.size() > settings.getSafetyFactor()){
            gainList.remove(0);
        }
    }

    public void setCandleList(List <BpCandlestick> _candleList){
        for (BpCandlestick bpCandlestick : _candleList) {
            this.addCandle(bpCandlestick);
        }
        log.debug("Set list:\n" + candleList);
    }

    public BpCandlestick getLastCandle(){
        if (candleList.size() < 1){
            log.debug("List is empty!");
            return null;
        }
        return candleList.get(candleList.size()-1);
    }

    public static double gain(final double before, final double after){
        return (after - before) / before;
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

    public double amountToSell(){
        BigDecimal bd = new BigDecimal(boughtFor * (1 - MAKER_FEE)).setScale(settings.getFromCurrency().getAmountPrecision(), RoundingMode.DOWN);
        return bd.doubleValue();
    }

    public void clearAll(){
        values.clear();
        rsi.clear();
        macd.clear();
        ema.clear();
        atr.clear();    
        cmf.clear(); 
    }
}