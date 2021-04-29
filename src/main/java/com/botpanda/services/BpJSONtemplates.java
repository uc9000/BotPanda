package com.botpanda.services;

import java.util.ArrayList;
import java.util.List;

import com.botpanda.entities.BpCandlestick;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@Slf4j
public class BpJSONtemplates {
    private boolean print = true;
    private String output;
    private Gson gson = new Gson();

    public void log(String output){
        output = new JSONObject(output).toString(4);
        if(print){
            log.info(output);
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

    public BpCandlestick parseCandle(String candleJSON){
        log.trace("Parsed:\n" + candleJSON);
        return gson.fromJson(candleJSON, BpCandlestick.class);
    }

    public List<BpCandlestick> parseCandleList(String candleListJSON){
        JSONArray ja = new JSONArray(candleListJSON);
        List<BpCandlestick> list = new ArrayList<BpCandlestick>();
        for(int i = 0; i < ja.length(); i++){
            list.add(parseCandle(ja.get(i).toString()));
        }
        log.trace("Parsed list: " + list);
        return list;
    }

    public String getJSONtype(String message){
        String type = new JSONObject(message).get("type").toString();
        log.debug("processed TYPE = " + type);
        return type;
    }
}