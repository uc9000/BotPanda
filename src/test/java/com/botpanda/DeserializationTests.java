package com.botpanda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.botpanda.components.connection.BpJSONtemplates;
import com.botpanda.entities.enums.TimeUnits;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;


@Slf4j
@SpringBootTest
public class DeserializationTests {
    private final BpJSONtemplates js = new BpJSONtemplates();

    @Test
    void parseCandlesticksChannelTest(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("testFiles/candlestickChannel.json")).getFile());
        JSONObject targetMsg, generatedMsg;
        try {
            String msg = new String(Files.readAllBytes(file.toPath()));
            targetMsg = new JSONObject(msg);
            generatedMsg = new JSONObject(js.subscriptionToCandles("BTC", "EUR", 5, TimeUnits.MINUTES.toString()));
            JSONAssert.assertEquals(targetMsg, generatedMsg, false);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
