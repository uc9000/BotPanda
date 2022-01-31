package com.botpanda.entities.enums;

import com.google.gson.annotations.SerializedName;

public enum TimeUnits{
    MINUTES(60),
    HOURS(3600);

    final int seconds;

    TimeUnits(int seconds){
        this.seconds = seconds;
    }
}
