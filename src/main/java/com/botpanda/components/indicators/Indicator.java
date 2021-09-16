package com.botpanda.components.indicators;

import java.util.ArrayList;

public interface Indicator {
    Double calc();
    boolean shouldBuy();
    boolean shouldSell();
    void setValues(ArrayList<Double> values);
    Double getLast();
    void clear();
}