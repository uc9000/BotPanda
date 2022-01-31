package com.botpanda.entities;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Subscribe {
    @Expose
    private final String type = "SUBSCRIBE";
    @Expose
    private ArrayList<Channel> channels = new ArrayList<>();

    public Subscribe forCandlesticks(){
        channels.add(new Channel());
        return this;
    }

    public Subscribe withProperty(Property property){
        channels.get(channels.size()-1).getProperties().add(property);
        return this;
    }
}
