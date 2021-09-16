package com.botpanda.controllers;

import com.botpanda.components.BotSettings;
import com.botpanda.components.BpConnectivity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class homeController {

    BotSettings settings = new BotSettings();
    @Autowired
    BpConnectivity bpConnection;


    @GetMapping("/start")
    public String home(){
        //settings.setTestingMode(true);
        bpConnection.setSettings(settings);
        bpConnection.connect();
        System.out.println(settings.toString());
        return "index";
    }

    @GetMapping("/auth")
    public String auth(@RequestParam(required = false, name = "key", defaultValue = "") String apiKey){
        bpConnection.authenticate(apiKey);
        return "index";
    }
    
    @GetMapping("/subToCandles" )
    public String subscribeToCandles(){        
        bpConnection.subscribeToCandles();
        return "index";
    }

    @GetMapping("/subToOrders")
    public String subscribeToOrders(){
        bpConnection.subscribeToOrders();
        return "index";
    }

    @GetMapping("/close")
    public String close(@RequestParam(name = "sell", defaultValue = "false", required = false) boolean sell){
        bpConnection.closeConnection(sell);
        return "index";
    }
}