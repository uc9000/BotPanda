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


    @GetMapping("/")
    public String home(){
        //bpConnecion.getAllCandles();
        bpConnecion.setSettings(settings);
        bpConnecion.connect();
        System.out.println(settings.toString());
        return "index";
    }

    // @GetMapping("/auth/{key}")
    // public String auth(@PathVariable(required = false, name = "key") String apiKey){
    //     try {
    //         if(apiKey == null || apiKey.length() < 4){
    //             bpConnecion.authenticate("");
    //         }
    //         else{
    //             bpConnecion.authenticate(apiKey);
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return "index.html";
    // }
    
    @GetMapping("/subscribecandles" )
    public String subscribeToCandles(){        
        bpConnecion.subscribeToCandles();
        return "index.html";
    }

    @GetMapping("/subscribeorders")
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