package com.botpanda.services;

import java.util.ArrayList;
import java.util.List;

import com.botpanda.entities.BpCandlestick;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotLogic {
    public List <BpCandlestick> candles = new ArrayList<BpCandlestick>();

    @Autowired
    BotSettings settings;

    private double RS(){
        double up = 0, down = 0;
        int i=0;
        double prevClose = 0;
        for(BpCandlestick candle : candles){
            if (i == 0){
                prevClose = candle.getClose();
                i++;
                continue;
            }
            double close = candle.getClose();
            if (close - prevClose < 0){
                down -= close - prevClose;
            }
            else{
                up += close - prevClose;
            }
            i++;
        }
        return up/down;
    }

    public double RSI(){
        if (candles.size() < 5){
            return 50;
        }
        return 100 - (100 / (1 + RS()));
    }
}
