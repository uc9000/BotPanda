package com.botpanda.entities.enums;

public enum TimeUnits{
    MINUTES(60),
    HOURS(3600);

    final int seconds;

    TimeUnits(int seconds){
        this.seconds = seconds;
    }
}
