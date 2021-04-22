package com.botpanda.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BpWebsocket {
    @Autowired
    private BpEndpoint endpoint;
    private WebSocketContainer container;

    public BpWebsocket(){
        this.container = ContainerProvider.getWebSocketContainer();
    }

    public void connect() throws DeploymentException, IOException, URISyntaxException{
        this.container.connectToServer(this.endpoint, new URI("wss://streams.exchange.bitpanda.com"));
    }
}