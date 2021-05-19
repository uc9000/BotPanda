package com.botpanda.entities;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Balance {
    @SerializedName("currency_code")
    String currencyCode;
    String change;
    String available;
    String locked;
}
