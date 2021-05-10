package com.botpanda.controllers;

import java.io.IOException;

import com.botpanda.services.BotSettings;
import com.botpanda.services.BpConnectivity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class homeController {

    BotSettings settings = new BotSettings();
    @Autowired
    BpConnectivity bpConnecion;


    @GetMapping("")
    public String home(){     
        //bpConnecion.getAllCandles();
        bpConnecion.setSettings(settings);
        bpConnecion.connect();
        System.out.println(settings.toString());
        return "index.html";
    }

    @GetMapping("/auth")
    public String auth(){
        try {
            bpConnecion.authenticate("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index.html";
    }
    
    @GetMapping("/subscribe/candles" )
    public String subscribeToCandles(){        
        bpConnecion.subscribeToCandles();
        return "index.html";
    }

    @GetMapping("/subscribe/orders")
    public String subscribeToOrders(){
        //bpConnecion.subscribeToOrders();
        return "index.html";
    }

    @GetMapping("/close")
    @ResponseBody    
    public String close(@RequestParam(name = "sell", defaultValue = "false", required = false ) boolean sell){
        if(sell){
            //TODO
        }
        return "index.html";
    }
}