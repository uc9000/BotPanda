package com.botpanda.services;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Service
public class BpJSONtemplates {
    @Setter
    @Getter
    private boolean print = true;
    private String output;

    private void log(String output){
        if(print){
            System.out.println(output);
        }
    }
    public String authentication(String key){
        output = new String("{\"type\": \"AUTHENTICATE\", \"api_token\": \"" + key + "\"}");
        log(output);
        return output;
    }
}
