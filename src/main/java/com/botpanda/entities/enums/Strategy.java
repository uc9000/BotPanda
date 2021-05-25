package com.botpanda.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Strategy {
    MACD_AND_EMA(false, true, true),
    RSI_AND_EMA(true, false, true),
    MACD_RSI_EMA(true, true, true);

    @Getter
    final boolean usingRsi, usingMacd, usingEma;
}
