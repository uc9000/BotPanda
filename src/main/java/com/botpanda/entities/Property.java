package com.botpanda.entities;

import com.botpanda.entities.enums.TimeGranularity;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Property {
    String instrument_code;
    TimeGranularity time_granularity;
}
