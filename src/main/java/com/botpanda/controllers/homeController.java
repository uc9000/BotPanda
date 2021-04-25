package com.botpanda.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;

import com.botpanda.services.BpConnectivity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    @Autowired
    BpConnectivity bpConnecion;

    @GetMapping("")
    public String home() throws DeploymentException, IOException, URISyntaxException{     
        bpConnecion.getAllCandles();
        bpConnecion.connect();
        return "index.html";
    }
}
