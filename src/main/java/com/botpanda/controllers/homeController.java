package com.botpanda.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;

import com.botpanda.services.BpGetCandles;
import com.botpanda.services.BpWebsocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    @Autowired
    BpGetCandles candles;
    @Autowired
    BpWebsocket ws;

    @GetMapping("")
    public String home() throws DeploymentException, IOException, URISyntaxException{     
        candles.getAllCandles();
        ws.connect();
        return "index.html";
    }
}
