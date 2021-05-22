package com.botpanda.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.botpanda.entities.BpCandlestick;
import com.botpanda.entities.Order;
import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.OrderSide;
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
    private String output;
    private Gson gson = new Gson();

    public String authentication(String key){
        output = new String("{\"type\": \"AUTHENTICATE\", \"api_token\": \"" + key + "\"}");
        log.debug(output);
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
        log.debug(output);
        return output;
    }

    public String subscriptionToOrders(){
        JSONObject jo = new JSONObject();
        jo.put("type", "SUBSCRIBE")
        .put("channels", new JSONArray()
            .put(new JSONObject().put("name", "ORDERS"))
        );
        output = jo.toString();
        log.debug(output);
        return output;
    }

    public BpCandlestick parseCandle(String candleJSON){
        log.trace("Parsed:\n" + candleJSON);
        return gson.fromJson(candleJSON, BpCandlestick.class);
    }

    public Order parseOrder(String orderJSON){
        return gson.fromJson(orderJSON, Order.class);
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

    public String createOrder(Currency fromCurrency, Currency toCurrency, OrderSide side, double amount){
        int precision = fromCurrency.getAmountPrecision();
        log.debug("Precision: " + precision);
        BigDecimal bd = new BigDecimal(amount).setScale(fromCurrency.getAmountPrecision(), RoundingMode.DOWN);
        String strAmount;
        if(precision == 0){
            strAmount = String.valueOf((int)amount);
        }else{
            strAmount = bd.toPlainString();
        }
        String strOrder = gson.toJson(new Order(new String(fromCurrency.name() + "_" + toCurrency.name()), side.name(), strAmount));
        JSONObject order = new JSONObject(strOrder);
        JSONObject json = new JSONObject()
            .put("type", "CREATE_ORDER")
            .put("order", order);
        log.debug(json.toString(4));
        return json.toString();
    }

    /*
    public String subscriptionToAccountHistory(){
        JSONObject jo = new JSONObject();
        jo
        .put("type", "SUBSCRIBE")
        .put("bp_remaining_quota", 200)
        .put("channels", new JSONArray()
            .put(new JSONObject().put("name", "ACCOUNT_HISTORY"))
        );
        output = jo.toString();
        log.debug(output);
        return output;
    }

    public Balance parseBalance(String balanceJSON, Currency currency){
        JSONArray balances = new JSONObject(balanceJSON).getJSONArray("balances");
        for(int i = 0; i < balances.length(); i++){
            Balance b = gson.fromJson(balances.get(i).toString(), Balance.class);
            if(b.getCurrencyCode().equals(currency.name())){
                log.info("balance: " + b.getAvailable());
                return b;
            }
        }
        return new Balance();
    }
    */
}