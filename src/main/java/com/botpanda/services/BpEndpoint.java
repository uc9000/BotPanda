package com.botpanda.services;

import javax.websocket.ClientEndpoint;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.springframework.stereotype.Component;

//wss://streams.exchange.bitpanda.com/candlesticks
@Component
@ClientEndpoint
public class BpEndpoint extends Endpoint{    
    private Session session;
    //URI url = new URI("wss://streams.exchange.bitpanda.com");
    @Override
    public void onOpen(Session session, EndpointConfig arg1) {
        this.session = session;
        this.session.addMessageHandler(new MessageHandler.Whole<String>(){
            @Override
            public void onMessage(String message) {
                System.out.println(message);                
            }
        });        
    }
}