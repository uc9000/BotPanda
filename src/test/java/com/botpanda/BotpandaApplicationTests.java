package com.botpanda;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.botpanda.entities.BpCandlestick;
import com.botpanda.services.BotLogic;
import com.botpanda.services.BotSettings;
import com.botpanda.services.BpConnectivity;
import com.botpanda.services.BpJSONtemplates;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class BotpandaApplicationTests {
	@Autowired
	BpConnectivity con;
	BotLogic bl = new BotLogic();
	@Autowired
	BpJSONtemplates js;
	BotSettings settings;

	@Test	
	void parseFromJsonTest() throws InterruptedException {
		//given
		settings = new BotSettings();
		con.setSettings(settings);
		bl.setSettings(settings);
		//when
		JSONArray candlesJsArr = new JSONArray(con.getAllCandles());
		log.info("Test: JSON Array:\n" + candlesJsArr.toString(4));
		List <BpCandlestick> list = js.parseCandleList(candlesJsArr.toString());
		log.info("Test: Java list:\n" + list.toString());
		bl.setCandleList(list);
		log.info("List in BL:\n" + bl.getCandleList().toString());
		//then
		assert(list).equals(bl.getCandleList());
	}

	@Test
	void rsiCalcTest(){
		//given
		settings = new BotSettings();	
		settings.setMaxCandles(8);
		bl.setSettings(settings);
		bl.addCandle(new BpCandlestick(100));
		bl.addCandle(new BpCandlestick(100));
		bl.addCandle(new BpCandlestick(110));
		bl.addCandle(new BpCandlestick(125));
		bl.addCandle(new BpCandlestick(110));
		bl.addCandle(new BpCandlestick(100));
		bl.addCandle(new BpCandlestick(90));
		bl.addCandle(new BpCandlestick(80));
		bl.addCandle(new BpCandlestick(65));
		//when
		int expectedRsi = (int)29.41f;
		int rsi =  (int)bl.getLastRsi();
		log.info("candleList of size: " + bl.getCandleList().size() + "\n = " + bl.getCandleList());
		log.info("rsi = " + rsi + " and expected = " + expectedRsi);
		log.info("RS = " + bl.getLastRs() + " ; avg loss = " + bl.getLastAvgLoss() + " and avg gain = " + bl.getLastAvgGain());
		//then
		assertTrue(rsi == expectedRsi);
	}
}