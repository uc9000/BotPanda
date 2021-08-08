package com.botpanda.components;

import java.util.ArrayList;

import com.botpanda.components.indicators.AverageTrueRange;

import lombok.Getter;
import lombok.Setter;

public class RiskManagement {
    @Getter
    private Double stopLossPrice = 0.0, targetPrice = 0.0;
    @Getter @Setter
    private Double entryPrice = 0.0;
    @Getter
    private Double entryAtr;
    @Setter
    private BotSettings settings;
    @Setter
    private AverageTrueRange atr;
    @Setter
    private ArrayList<Double> values;

    public RiskManagement(){
        throw new UnsupportedOperationException("Deleted default constructor for RiskManagement class");
    }

    public RiskManagement(BotSettings settings, AverageTrueRange atr, ArrayList<Double> values){
        this.setSettings(settings);
        this.setValues(values);
        this.setAtr(atr);
    }

    private boolean hardTargetReached(){
        if(entryPrice * (settings.getTarget() + 1) < values.get(values.size()-1)){
            return true;
        }
        return false;
    }

    private boolean atrTargetReached(){
        if(settings.getAtrTarget() * entryAtr + entryPrice < values.get(values.size()-1)){
            return true;
        }
        return false;
    }

    private boolean hardStopLossReached(){
        if(entryPrice * (1 - settings.getStopLoss()) > values.get(values.size()-1)){
            return true;
        }
        return false;
    }

    private boolean atrStopLossReached(){
        if(entryPrice - (settings.getAtrStopLoss() * entryAtr) > values.get(values.size()-1)){
            return true;
        }
        return false;
    }

    public boolean targetReached(){
        if(hardTargetReached() || atrTargetReached()){
            return true;
        }        
        return false;
    }

    public boolean stopLossReached(){
        if(hardStopLossReached() || atrStopLossReached()){
            return true;
        }
        return false;
    }
}