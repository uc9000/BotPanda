package com.botpanda.controllers;

import java.io.IOException;

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


    @GetMapping("/")
    public String home(){
        bpConnecion.setSettings(settings);
        bpConnecion.connect();
        System.out.println(settings.toString());
        return "index";
    }

    @GetMapping("/auth")
    public String auth(@RequestParam(required = false, name = "key", defaultValue = "") String apiKey){
        try {
            bpConnecion.authenticate(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index.html";
    }
    
    @GetMapping("/subToCandles" )
    public String subscribeToCandles(){        
        bpConnecion.subscribeToCandles();
        return "index.html";
    }

    @GetMapping("/subToOrders")
    public String subscribeToOrders(){
        bpConnecion.subscribeToOrders();
        return "index.html";
    }

    @GetMapping("/close")
    public String close(@RequestParam(name = "sell", defaultValue = "false", required = false) boolean sell){
        if(sell){
            //TODO
        }
        return "index.html";
    }
}