package com.botpanda.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Strategy {
    MACD_AND_EMA(false, true,  true,  false),
    RSI_AND_EMA (true,  false, true,  false),
    MACD_RSI_EMA(true,  true,  true,  false),
    MACD_CMF    (false, true,  false, true);

    @Getter
    final boolean usingRsi, usingMacd, usingEma, usingCmf;
}
