package com.botpanda.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletionStage;

import org.springframework.stereotype.Service;

import lombok.EqualsAndHashCode;
import lombok.Getter;

//GET CANDLES FROM REST API AND WEBSOCKETS
@Service
@EqualsAndHashCode
public class BpConnectivity {
    //REST VARS
    private static final String REST_URL = "https://api.exchange.bitpanda.com/public/v1/candlesticks/";
    private static final String WS_URL = "wss://streams.exchange.bitpanda.com";
    private String restUrl;
    private OffsetDateTime date = OffsetDateTime.now(ZoneOffset.UTC);
    private int maxAmount = 15;

    //WEBSOCKET VARS
    @Getter
    WebSocket ws;
    Listener wsListener = new Listener(){
        @Override
        public void onOpen(WebSocket webSocket){
            System.out.println("\nOPENED!");
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason){
            System.out.println("CLOSED \nstatus code: " + statusCode + "\n reason: " + reason);
            return null;
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
            System.out.println("message: " + message);
            return null;
        }
    };
    //private

    //REST API METHODS
    public String getAllCandles(){
        String instrumentCodes = "DOGE_EUR?";
        String unit = "MINUTES";
        int period = 5;
        OffsetDateTime fromDate = date.minusMinutes((maxAmount + 1) * period);
        String fromDateStr = URLEncoder.encode(fromDate.toString(), StandardCharsets.UTF_8);
        String params = new String(instrumentCodes 
        + "unit=" + unit + "&period=" + period 
        + "&from=" + fromDateStr + "&to=" + URLEncoder.encode(date.toString(), StandardCharsets.UTF_8));
        this.restUrl = new String(REST_URL + params);
        System.out.println(restUrl);
        StringBuffer response = new StringBuffer();
        try{
            URL url = new URL(restUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            return response.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new String("GET CANDLES FAILED");
    }
    //WEBSOCKETS METHODS
    public void connect() throws URISyntaxException{
        ws = HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(60)).buildAsync(new URI(WS_URL), wsListener).join();
    }
}