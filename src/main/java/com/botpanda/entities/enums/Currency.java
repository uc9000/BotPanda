package com.botpanda.entities.enums;

import lombok.Getter;

/*
Enums that store it's currency code in the name() and precision as decimal places
*/
public enum Currency {
    EUR(2),
    USD(2),
    BTC(8),
    ETH(8),
    DOGE(8),
    XRP(8),
    LTC(8),
    MIOTA(8),
    BEST(8),
    LINK(8),
    PAN(8),
    DOT(8),
    XLM(8),
    CHZ(8);

    @Getter
    private final int precision;
    Currency(int precision){
        this.precision = precision;
    }
}
