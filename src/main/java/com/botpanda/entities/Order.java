package com.botpanda.entities;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Order {
    @SerializedName(value = "instrument_code")
    String instrumentCode;
    String type;
    String side;
    String amount;

    public Order(){
        type = "MARKET";
        amount = "0.00001";
    }

    public Order(String instrumentCode, String side, String amount){
        this();
        this.instrumentCode = instrumentCode;
        this.side = side;
        this.amount = amount;
    }
}
