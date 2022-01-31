package com.botpanda;

import com.botpanda.components.connection.BpJSONtemplates;
import com.botpanda.entities.BpCandlestick;
import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.TimeGranularity;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
@SpringBootTest
public class DeserializationTests {
    private final BpJSONtemplates js = new BpJSONtemplates();

    @Test
    void serializeSubscriptionToCandlesticksChannelTest(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("testFiles/candlestickChannel.json")).getFile());
        JSONObject targetMsg, generatedMsg;
        try {
            String msg = new String(Files.readAllBytes(file.toPath()));
            targetMsg = new JSONObject(msg);
            generatedMsg = new JSONObject(js.subscriptionToCandles(Currency.BTC, Currency.EUR, TimeGranularity.MINUTES5));
            JSONAssert.assertEquals(targetMsg, generatedMsg, false);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void deserializeCandlestickChannelTest(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("testFiles/candlestickDeserializationExample.json")).getFile());
        String msg = "";
        try {
            msg = new String(Files.readAllBytes(file.toPath()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        BpCandlestick candle = js.parseCandle(msg);
        assertEquals(candle.getGranularity(), TimeGranularity.MINUTES5);
        assertEquals(candle.getClose(), 3900.12);

    }
}
