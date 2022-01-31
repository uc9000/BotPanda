package com.botpanda.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Channel {
    private String name = "CANDLESTICKS";
    private List<Property> properties = new ArrayList<>();
}
