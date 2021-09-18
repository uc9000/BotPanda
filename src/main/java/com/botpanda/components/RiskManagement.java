package com.botpanda.components;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class RiskManagement {
    @Getter
    private Double stopLossPrice = 0.0, targetPrice = 0.0;
    @Getter @Setter
    private Double entryPrice = 0.0;
    @Getter @Setter
    private Double entryAtr;
    @Setter
    private BotSettings settings;
    @Setter
    private ArrayList<Double> values;


    public void setReferences(BotSettings settings, ArrayList<Double> values){
        this.setSettings(settings);
        this.setValues(values);
    }

    private double lastValue(){
        return values.get(values.size() - 1);
    }

    private boolean hardTargetReached(){
        return entryPrice * (settings.getTarget() + 1) < lastValue();
    }

    private boolean atrTargetReached(){
        return settings.getAtrTarget() * entryAtr + entryPrice < lastValue();
    }

    private boolean hardStopLossReached(){
        return entryPrice * (1 - settings.getStopLoss()) > lastValue();
    }

    private boolean atrStopLossReached(){
        return entryPrice - (settings.getAtrStopLoss() * entryAtr) > lastValue();
    }

    public boolean targetReached(){
        return hardTargetReached() || atrTargetReached();
    }

    public boolean stopLossReached(){
        return hardStopLossReached() || atrStopLossReached();
    }
}