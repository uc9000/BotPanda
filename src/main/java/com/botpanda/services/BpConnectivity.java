package com.botpanda.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import java.nio.file.Files;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletionStage;

import com.botpanda.entities.enums.OrderSide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

//GET CANDLES FROM REST API AND WEBSOCKETS
@Service
@Slf4j
public class BpConnectivity {
    @Getter
    private boolean connected = false, authenticated = false,  subscribedToOrders = false, subscribedToCandles = false; 
    private BotLogic botLogic = new BotLogic();
    private BotSettings settings = new BotSettings();

    @Autowired
    private BpJSONtemplates jsonTemplate;

    public void setSettings(BotSettings settings){
        this.settings = settings;
        botLogic.setSettings(settings);
    }

    //CONSTRUCTORS:
    public BpConnectivity(){
        settings = new BotSettings();
        botLogic.setSettings(settings);
    }

    //REST VARS
    private static final String REST_URL = "https://api.exchange.bitpanda.com/public/v1/candlesticks/";
    private String restUrl;
    private OffsetDateTime date = OffsetDateTime.now(ZoneOffset.UTC);

    
    //WEBSOCKET VARS
    private static final String WS_URL = "wss://streams.exchange.bitpanda.com";
    private WebSocket ws;
    private String apiKey;

    Listener wsListener = new Listener(){
        @Override
        public void onOpen(WebSocket webSocket){
            connected = true;
            webSocket.request(1);
            log.info("\nOPENED with subprotocol: " + webSocket.getSubprotocol());
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason){
            log.warn("CLOSED \nstatus code: " + statusCode + "\n reason: " + reason);
            reconnect();
            return null;
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
            webSocket.request(1);
            //log.info(message.toString());
            String type = jsonTemplate.getJSONtype(message.toString());
            if(!type.equals("HEARTBEAT")){ // print everything except heartbeats
                log.info("received message: " + message.toString());
            }
            if(type.equals("SUBSCRIPTIONS")){
                JSONArray channels = new JSONObject(message.toString()).getJSONArray("channels");
                for (int i = 0; i < channels.length() ; i++){
                    JSONObject jo = new JSONObject(channels.get(i).toString());
                    if(jo.get("name").equals("ORDERS")){
                        subscribedToOrders = true;
                    }
                }
            }
            if(type.equals("AUTHENTICATED")){
                authenticated = true;
            }
            if(type.equals("CANDLESTICK") || type.equals("CANDLESTICK_SNAPSHOT")){
                botLogic.addCandle(jsonTemplate.parseCandle(message.toString()));
                if(botLogic.shouldBuy()){
                    sendMarketOrder(OrderSide.BUY, botLogic.amount());
                    log.warn(
                        "BUYING " + botLogic.amount() + " " 
                        + settings.getToCurrency().name() + " at price: " 
                        + botLogic.getBuyingPrice()
                    );
                    botLogic.setBought(true);
                }
                else if(botLogic.shouldSell()){
                    sendMarketOrder(OrderSide.SELL, botLogic.amount());
                    log.warn(
                        "SELLING " + botLogic.amount() + " " + settings.getToCurrency().name() 
                        + " at price: " + botLogic.getSellingPrice() 
                        + "  with gain [%] : " + 100 * botLogic.currentGain()
                    );
                    botLogic.setBought(false);
                }
                else if (botLogic.isBought()){
                    log.info("HOLD. Current gain [%]: " + 100 * botLogic.currentGain());
                }
                else{
                    log.info("WAIT WITH BUYING");
                }
            }
            return null;
        }
    };

    //REST API METHODS
    public String getAllCandles(){
        String instrumentCodes = new String(settings.getFromCurrency() + "_" + settings.getToCurrency() + "?");
        OffsetDateTime fromDate = date.minusMinutes(settings.getMaxCandles() * 4 * settings.getPeriod());
        String fromDateStr = URLEncoder.encode(fromDate.toString(), StandardCharsets.UTF_8);
        String params = new String(instrumentCodes 
        + "unit=" + settings.getUnit() + "&period=" + settings.getPeriod()
        + "&from=" + fromDateStr + "&to=" + URLEncoder.encode(date.toString(), StandardCharsets.UTF_8));
        this.restUrl = new String(REST_URL + params);
        //System.out.println(restUrl);
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
            //log.info("GET candles REST response:\n" + response.toString());
            return response.toString();
        }catch (Exception e){
            e.printStackTrace();
            
        }
        return new String("GET CANDLES FAILED");
    }
    
    //WEBSOCKETS METHODS
    public void connect(){
        try {
            ws = HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(20)).buildAsync(new URI(WS_URL), wsListener).join();
        } catch (URISyntaxException e) {
            connected = false;
            e.printStackTrace();
        }
    }

    public void authenticate(String apiKey){
        if(apiKey.length() < 1){
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("API.private").getFile());            
            try {
                apiKey = new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                log.error("Authentication error: API key not provided");
            }
        }else{
            this.apiKey = apiKey;
        }
        ws.sendText(jsonTemplate.authentication(apiKey), true);
    }

    public void subscribeToCandles(){
        if(!connected){
            log.warn("Can't subscribe to candles, not connected yet");
            return;
        }
        botLogic.setCandleList(jsonTemplate.parseCandleList(getAllCandles()));
        ws.sendText(
            jsonTemplate.subscribtionToCandles(
                settings.getFromCurrency().name(),
                settings.getToCurrency().name(),
                settings.getPeriod(),
                settings.getUnit().name()
            ),
            true
        );        
    }

    public void subscribeToOrders(){
        if(!authenticated){
            log.warn("Can't subscribe to orders, not authenticated yet");
            return;
        }
        ws.sendText(
            jsonTemplate.subscriptionToOrders(),
            true
        );
    }

    public void sendMarketOrder(OrderSide side, double amount){
        if(!subscribedToOrders){
            log.warn("To make orders subscribe to order first!");
            return;
        }
        String msg = jsonTemplate.createOrder(settings.getFromCurrency(), settings.getToCurrency(), side, amount);
        log.info("sending request:\n" + msg);
        //TODO : uncomment after testing
        //ws.sendText(msg, true);
    }

    public void closeConnection(boolean sell){
        sendMarketOrder(OrderSide.SELL, botLogic.amount());
        ws.abort();
        connected = false;
        authenticated = false;
        subscribedToCandles = false;
        subscribedToOrders = false;
    }

    public void reconnect(){
        if(connected){
            connect();
        }
        else{
            return;
        }     
        if(authenticated){
            authenticate(apiKey);
        }
        if(subscribedToCandles){
            subscribeToCandles();
        }
        if(subscribedToOrders){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            subscribeToOrders();
        }
    }
}