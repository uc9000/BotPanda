package com.botpanda.entities.enums;

import lombok.Getter;

public enum TimeGranularity{
    MINUTES1  (TimeUnits.MINUTES, 1),
    MINUTES5  (TimeUnits.MINUTES, 5),
    MINUTES15 (TimeUnits.MINUTES, 15),
    MINUTES30 (TimeUnits.MINUTES, 30),
    HOURS1    (TimeUnits.HOURS,   1),
    HOURS4    (TimeUnits.HOURS,   4);

    @Getter
    TimeUnits unit;
    @Getter
    int period;

    TimeGranularity(TimeUnits unit, int period){
        this.unit = unit;
        this.period = period;
    }

    public int getSeconds(){
        return this.period * unit.seconds;
    }

    public int getMinutes(){
        return getSeconds() / 60;
    }
}