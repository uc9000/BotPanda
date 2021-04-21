package com.botpanda.controllers;

import com.botpanda.services.BpGetCandles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    @Autowired
    BpGetCandles candles;

    @GetMapping("")
    public String home(){     
        candles.getAllCandles();
        return "index.html";
    }
}
