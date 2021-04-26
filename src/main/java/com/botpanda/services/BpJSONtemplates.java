package com.botpanda.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Service
public class BpJSONtemplates {
    @Setter
    @Getter
    private boolean print = true;
    private String output;

    public void log(String output){
        output = new JSONObject(output).toString(4);
        if(print){
            System.out.println(output);
        }
    }

    public String authentication(String key){
        output = new String("{\"type\": \"AUTHENTICATE\", \"api_token\": \"" + key + "\"}");
        log(output);
        return output;
    }

    public String subscribtionToCandles(String fromCurrency, String toCurrency, int period, String unit){
        JSONObject jo = new JSONObject();
        jo.put("type", "SUBSCRIBE")
        .put("channels", new JSONArray()
            .put(new JSONObject().put("name", "CANDLESTICKS")
            .put("properties", new JSONArray()
                .put(new JSONObject()
                    .put("instrument_code", new String(fromCurrency + "_" + toCurrency))
                    .put("time_granularity", new JSONObject()
                        .put("unit", unit)
                        .put("period", period)
                    ) 
                )
            ))
        );
        output = jo.toString();
        log(output);
        return output;
    }
}