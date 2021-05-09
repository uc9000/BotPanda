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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

//GET CANDLES FROM REST API AND WEBSOCKETS
@Service
@Slf4j
public class BpConnectivity {
    @Getter
    private boolean connected = false;
    @Getter
    private boolean authenticated = false;    
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

    Listener wsListener = new Listener(){
        @Override
        public void onOpen(WebSocket webSocket){
            connected = true;
            authenticated = false;
            webSocket.request(1);
            log.info("\nOPENED with subprotocol: " + webSocket.getSubprotocol());
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason){
            log.warn("CLOSED \nstatus code: " + statusCode + "\n reason: " + reason);
            connected = false;
            authenticated = false;
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
            if(type.equals("AUTHENTICATED")){
                authenticated = true;
            }
            //Not necessery just for candlestick subscription
            //Later change that to api key given from gui
            // if(!authenticated){
            //     try {
            //         authenticate("");
            //     } catch (IOException e) {
            //         e.printStackTrace();
            //     }
            // }
            if(type.equals("CANDLESTICK") || type.equals("CANDLESTICK_SNAPSHOT")){
                botLogic.addCandle(jsonTemplate.parseCandle(message.toString()));
                if(botLogic.shouldBuy()){
                    log.warn("BUYING at price: " + botLogic.getBuyingPrice());
                    botLogic.setBought(true);
                }
                else if(botLogic.shouldSell()){
                    log.warn("SELLING at price: " + botLogic.getSellingPrice() + "  with gain [%] : " + 100 * botLogic.currentGain());
                    botLogic.setBought(false);
                }
                else if (botLogic.isBought()){
                    log.warn("HOLD. Current gain [%]: " + 100 * botLogic.currentGain());
                }
                else{
                    log.warn("WAIT WITH BUYING");
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

    public void authenticate(String key) throws IOException{
        if(key.length() < 1){
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("API.private").getFile());
            key = new String(Files.readAllBytes(file.toPath()));
        }
        ws.sendText(jsonTemplate.authentication(key), true);
        ws.request(1);
    }

    public void subscribe(){
        if(!connected){
            log.warn("Can't subscribe, not connected yet");
            return;
        }
        ws.sendText(jsonTemplate.subscribtionToCandles(settings.getFromCurrency(), settings.getToCurrency(), settings.getPeriod(), settings.getUnit()), true);
        ws.request(1);
        botLogic.setCandleList(jsonTemplate.parseCandleList(getAllCandles()));
    }
}