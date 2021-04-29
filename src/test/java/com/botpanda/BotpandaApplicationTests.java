package com.botpanda;

import java.util.List;

import com.botpanda.entities.BpCandlestick;
import com.botpanda.services.BotLogic;
import com.botpanda.services.BotSettings;
import com.botpanda.services.BpConnectivity;
import com.botpanda.services.BpJSONtemplates;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BotpandaApplicationTests {
	@Autowired
	BpConnectivity con;
	BotLogic bl = new BotLogic();
	@Autowired
	BpJSONtemplates js;
	Logger log = LoggerFactory.getLogger(BotpandaApplicationTests.class);
	BotSettings settings;

	@Test
	void ParseFromJsonTest() throws InterruptedException {
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
}
