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

//GET CANDLES FROM REST API AND WEBSOCKETS
@Service
public class BpConnectivity {
    @Getter
    private boolean connected = false;
    @Getter
    private boolean authenticated = false;

    @Autowired
    private BotLogic botLogic;

    @Autowired
    private BotSettings settings;

    @Autowired
    private BpJSONtemplates jsonTemplate;

    //REST VARS
    private static final String REST_URL = "https://api.exchange.bitpanda.com/public/v1/candlesticks/";
    private static final String WS_URL = "wss://streams.exchange.bitpanda.com";
    private String restUrl;
    private OffsetDateTime date = OffsetDateTime.now(ZoneOffset.UTC);

    
    //WEBSOCKET VARS
    private WebSocket ws;
    Listener wsListener = new Listener(){
        @Override
        public void onOpen(WebSocket webSocket){
            webSocket.request(1);
            System.out.println("\nOPENED with subprotocol: " + webSocket.getSubprotocol());
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason){
            System.out.println("CLOSED \nstatus code: " + statusCode + "\n reason: " + reason);
            connected = false;
            authenticated = false;
            return null;
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
            webSocket.request(1);
            System.out.println("received message: ");
            String type = jsonTemplate.getJSONtype(message.toString());
            if(!type.equals("HEARTBEAT")){ // don't print heartbeats
                jsonTemplate.log(message.toString());
                if(authenticated){

                }
            }
            if(type.equals("AUTHENTICATED")){
                authenticated = true;
            }
            if(!authenticated){
                try {
                    authenticate("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    };

    //REST API METHODS
    public String getAllCandles(){
        String instrumentCodes = new String(settings.getFromCurrency() + "_" + settings.getToCurrency() + "?");
        OffsetDateTime fromDate = date.minusMinutes((settings.getMaxCandles() + 1) * settings.getPeriod());
        String fromDateStr = URLEncoder.encode(fromDate.toString(), StandardCharsets.UTF_8);
        String params = new String(instrumentCodes 
        + "unit=" + settings.getUnit() + "&period=" + settings.getPeriod()
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
    public void connect(){
        try {
            ws = HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(20)).buildAsync(new URI(WS_URL), wsListener).join();
            connected = true;
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
            //System.out.println("key: " + key);
        }
        ws.sendText(jsonTemplate.authentication(key), true);
        ws.request(1);
        //authenticated = true;
    }

    public void subscribe(){
        if(!connected || !authenticated){
            System.out.println("Can't subscribe, not connected/authenticated yet");
            return;
        }
        ws.sendText(jsonTemplate.subscribtionToCandles(settings.getFromCurrency(), settings.getToCurrency(), settings.getPeriod(), settings.getUnit()), true);
        ws.request(1);
        botLogic.candles = jsonTemplate.parseCandleList(getAllCandles());
    }
}