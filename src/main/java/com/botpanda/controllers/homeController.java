package com.botpanda.controllers;

import com.botpanda.services.BotSettings;
import com.botpanda.services.BpConnectivity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class homeController {

    BotSettings settings = new BotSettings();
    @Autowired
    BpConnectivity bpConnecion;


    @GetMapping("/start")
    public String home(){
        settings.setTestingMode(true);
        bpConnecion.setSettings(settings);
        bpConnecion.connect();
        System.out.println(settings.toString());
        return "index";
    }

    @GetMapping("/auth")
    public String auth(@RequestParam(required = false, name = "key", defaultValue = "") String apiKey){
        bpConnecion.authenticate(apiKey);
        return "index";
    }
    
    @GetMapping("/subToCandles" )
    public String subscribeToCandles(){        
        bpConnecion.subscribeToCandles();
        return "index";
    }

    @GetMapping("/subToOrders")
    public String subscribeToOrders(){
        bpConnecion.subscribeToOrders();
        return "index";
    }

    @GetMapping("/close")
    public String close(@RequestParam(name = "sell", defaultValue = "false", required = false) boolean sell){
        bpConnecion.closeConnection(sell);
        return "index";
    }
}