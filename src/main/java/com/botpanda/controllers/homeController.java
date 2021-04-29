package com.botpanda.controllers;

import java.io.IOException;

import com.botpanda.services.BotSettings;
import com.botpanda.services.BpConnectivity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    BotSettings settings = new BotSettings();
    BpConnectivity bpConnecion = new BpConnectivity();

    public homeController(){
        bpConnecion.setSettings(settings);
    }

    @GetMapping("")
    public String home(){     
        //bpConnecion.getAllCandles();
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

    
    @GetMapping("/subscribe" )
    public String subscribe(){        
        bpConnecion.subscribe();
        return "index.html";
    }
}
