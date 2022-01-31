package com.botpanda.components.connection;

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
import java.util.Objects;
import java.util.concurrent.CompletionStage;

import com.botpanda.components.BotLogic;
import com.botpanda.components.BotSettings;
import com.botpanda.entities.Order;
import com.botpanda.entities.enums.OrderSide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

//GET CANDLES FROM REST API AND WEBSOCKETS, SEND AND RECEIVE JSON MESSAGES
@Service
@Slf4j
public class BpConnectivity {
    //@Getter
    private boolean connected = false,
    authenticated = false,
    subscribedToOrders = false,
    subscribedToCandles = false,
    reconnecting = false;
    private final BotLogic botLogic = new BotLogic();
    private BotSettings settings;

    @Autowired
    private final BpJSONtemplates jsonTemplate;

    public void setSettings(BotSettings settings){
        this.settings = settings;
        botLogic.setSettings(settings);
    }

    //CONSTRUCTORS:
    public BpConnectivity(){
        this.jsonTemplate = new BpJSONtemplates();
        settings = new BotSettings();
        botLogic.setSettings(settings);
    }

    //REST VARS
    private static final String REST_URL = "https://api.exchange.bitpanda.com/public/v1/candlesticks/";

    //WEBSOCKET VARS
    private static final String WS_URL = "wss://streams.exchange.bitpanda.com";
    private WebSocket ws;
    private String apiKey = "";

    //OTHER VARS
    private OffsetDateTime date = OffsetDateTime.now(ZoneOffset.UTC);

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
            connected = false;
            subscribedToOrders = false;
            authenticated = false;
            subscribedToCandles = false;
            reconnecting = true;
            connect();            
            return null;
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
            webSocket.request(1);
            date = OffsetDateTime.now(ZoneOffset.UTC);
            if (reconnecting){
                if(settings.isTestingMode() && !subscribedToCandles){
                    subscribeToCandles();
                    reconnecting = false;
                }
                else if(!authenticated){
                    authenticate(apiKey);
                }
            }
            String strMsg = message.toString();
            String type = jsonTemplate.getJSONtype(strMsg);
            if(!type.equals("HEARTBEAT")
                    && !type.equals("CANDLESTICK")
            ){ // print everything except heartbeats
                log.info("received message: " + new JSONObject(strMsg).toString(4));
            }
            if(type.equals("CANDLESTICK") || type.equals("CANDLESTICK_SNAPSHOT")){                
                botLogic.addCandle(jsonTemplate.parseCandle(strMsg));
                handleNewCandle();
            }
            else if(!settings.isTestingMode() && type.equals("ORDER_CREATED")){
                Order order = jsonTemplate.parseOrder(new JSONObject(strMsg).get("order").toString());
                botLogic.setBought(order.getSide().equals(OrderSide.BUY.name()));
            }
            else if(type.equals("SUBSCRIPTIONS")){
                JSONArray channels = new JSONObject(strMsg).getJSONArray("channels");
                for (int i = 0; i < channels.length() ; i++){
                    JSONObject jo = new JSONObject(channels.get(i).toString());
                    if(jo.get("name").equals("ORDERS")){
                        log.info("Subscribed to orders");
                        subscribedToOrders = true;
                        if (!subscribedToCandles && reconnecting){
                            subscribeToCandles();
                        }
                    }
                    else if(jo.get("name").equals("CANDLESTICKS")){
                        log.info("Subscribed to candles");
                        subscribedToCandles = true;
                        reconnecting = false;
                    }
                }
            }
            else if(type.equals("AUTHENTICATED")){
                authenticated = true;
                if(!subscribedToOrders && reconnecting){
                    subscribeToOrders();
                }
            }
            return null;
        }
    };

    //REST API METHODS
    public String getAllCandles(){
        String instrumentCodes = settings.getFromCurrency() + "_" + settings.getToCurrency() + "?";
        OffsetDateTime fromDate = date.minusMinutes((long) settings.getMaxCandles() * 4 * settings.getTimeGranularity().getMinutes());
        String fromDateStr = URLEncoder.encode(fromDate.toString(), StandardCharsets.UTF_8);
        String params = instrumentCodes
                + "unit=" + settings.getTimeGranularity().getUnit().name() + "&period=" + settings.getTimeGranularity().getPeriod()
                + "&from=" + fromDateStr + "&to=" + URLEncoder.encode(date.toString(), StandardCharsets.UTF_8);
        String restUrl = REST_URL + params;
        //log(restUrl);
        StringBuilder response = new StringBuilder();
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
        return "GET CANDLES FAILED";
    }
    
    //WEBSOCKETS METHODS
    public void connect(){
        botLogic.clearAll();
        reconnecting = true;
        try {
            ws = HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(11)).buildAsync(new URI(WS_URL), wsListener).join();
        } catch (URISyntaxException e) {
            connected = false;
            e.printStackTrace();
        }
    }

    public void authenticate(String apiKey){
        if(apiKey.length() < 10){
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("API.private")).getFile());
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
            jsonTemplate.subscriptionToCandles(
                settings.getFromCurrency(),
                settings.getToCurrency(),
                settings.getTimeGranularity()
            ),true);
    }

    public void subscribeToOrders(){
        if(!authenticated){
            log.warn("Can't subscribe to orders, not authenticated yet");
            return;
        }
        ws.sendText(jsonTemplate.subscriptionToOrders(),true);
    }

    public void sendMarketOrder(OrderSide side, double amount){
        if(!subscribedToOrders){
            log.warn("To make orders subscribe to order first!");
            return;
        }
        String msg = jsonTemplate.createOrder(settings.getFromCurrency(), settings.getToCurrency(), side, amount);
        log.info("sending request:\n" + msg);
        if(!settings.isTestingMode()){
            ws.sendText(msg, true);
        }        
    }

    public void closeConnection(boolean sell){
        if(sell){
            sendMarketOrder(OrderSide.SELL, botLogic.amountToBuy());
        }
        ws.abort();
        connected = false;
        authenticated = false;
        subscribedToCandles = false;
        subscribedToOrders = false;
    }

    public void handleNewCandle(){
        if(botLogic.shouldBuy()){
            if(!settings.isTestingMode()){
                sendMarketOrder(OrderSide.BUY, botLogic.amountToBuy());
            }else {
                botLogic.setBought(true);
            }
            return;
        }
        if(botLogic.shouldSell()){
            if(!settings.isTestingMode()){
                sendMarketOrder(OrderSide.SELL, botLogic.amountToSell());
            }else {
                botLogic.setBought(false);
            }
            return;
        }
        if (botLogic.isBought()){
            log.info("HOLD. Current gain [%]: " + 100 * botLogic.currentGain());
        }
//        else if (botLogic.getLastCandle().getGranularity().equals(settings.getTimeGranularity())){
//            log.info("WAIT WITH BUYING");
//        }
    }
}