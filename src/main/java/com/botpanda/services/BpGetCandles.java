package com.botpanda.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class BpGetCandles {
    private String sURL = new String("https://api.exchange.bitpanda.com/public/v1/candlesticks/");
    private OffsetDateTime date = OffsetDateTime.now(ZoneOffset.UTC);
    private int maxAmount = 15;

    public void getAllCandles(){
        String instrumentCodes = "DOGE_EUR?";
        String unit = "MINUTES";
        int period = 5;
        OffsetDateTime fromDate = date.minusMinutes((maxAmount + 1) * period);
        String fromDateStr = URLEncoder.encode(fromDate.toString(), StandardCharsets.UTF_8);
        String params = new String(instrumentCodes 
        + "unit=" + unit + "&period=" + period 
        + "&from=" + fromDateStr + "&to=" + URLEncoder.encode(date.toString(), StandardCharsets.UTF_8));
        this.sURL = new String(sURL + params);
        System.out.println(sURL);
        //System.out.println(fromDate.toString() + "     " + date.toString());
        //int responseCode;
        try{
            URL url = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}