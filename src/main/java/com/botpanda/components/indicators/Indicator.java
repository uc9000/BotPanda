package com.botpanda.components.indicators;

import java.util.ArrayList;

public interface Indicator {
    public Double calc();
    public boolean shouldBuy();
    public boolean shouldSell();
    public void setValues(ArrayList<Double> values);
    public Double getLast();
    public void clear();
}