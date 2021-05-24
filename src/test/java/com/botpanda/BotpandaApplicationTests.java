package com.botpanda;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.botpanda.components.BotLogic;
import com.botpanda.components.BotSettings;
import com.botpanda.components.BpConnectivity;
import com.botpanda.components.BpJSONtemplates;
import com.botpanda.entities.BpCandlestick;
import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.OrderSide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class BotpandaApplicationTests {
	@Autowired
	private BpConnectivity con;
	private static BotLogic bl = new BotLogic();	
	@Autowired
	private BpJSONtemplates js;
	private static BotSettings settings;

	@BeforeAll
	public static void init(){
		settings = new BotSettings();
		bl.setSettings(settings);
		bl.rsi.setRsiLength(8);
		double blArr[] = {100, 100, 110,125, 110, 100, 90, 80, 65};
		for(double c : blArr){
			bl.addCandle(new BpCandlestick(c));
		}
	}

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
		if(list.size() == bl.getCandleList().size()){
			assert(list.toString()).equals(bl.getCandleList().toString());
		}else{
			assertTrue(bl.getCandleList().size() == settings.getMaxCandles() + 1);
		}		
	}

	@Test
	void rsiCalcTest(){
		//when
		int expectedvalue = (int)29.41f;
		int value =  (int)bl.rsi.calc().doubleValue();
		log.info("candleList of size: " + bl.getCandleList().size() + "\n = " + bl.getCandleList());
		log.info("values list in RSI: " + bl.rsi.getValues().toString());
		log.info("value = " + value + " and expected = " + expectedvalue);
		log.info("RS = " + bl.rsi.getLastRs() + " ; avg loss = " + bl.rsi.getLastAvgLoss() + " and avg gain = " + bl.rsi.getLastAvgGain());
		//then
		assertTrue(value == expectedvalue);
	}

	@Test
	void orderJsonTest(){
		String jsStr = js.createOrder(Currency.BTC, Currency.EUR, OrderSide.BUY, 43.9123466446653245);
		JSONObject json = new JSONObject(jsStr);
		JSONObject order = json.getJSONObject("order");
		log.info(json.toString(4));
		//log.info(order.toString(4));
		//assert(order.get("instrument_code")).equals("DOGE_EUR");
		assert(order.get("type")).equals("MARKET");
		assert(order.get("amount")).equals("43.91234");
	}

	@Test
	void isCrashingTest(){
		BotLogic botCrashing = new BotLogic();
		double crashingArr[] = 
		{
			100, 178, 76, 125, 110, 100, 101, 99, 112, 107,
			98, 88, 80, 65, 76, 45, 55, 70, 50, 55, 90, 45
		};
		for(double c : crashingArr){
			botCrashing.addCandle(new BpCandlestick(c));
		}
		assertTrue(botCrashing.isCrashing());
	}

	@Test
	void isNotCrashingTest(){
		BotLogic botNotCrashing = new BotLogic();
		double crashingArr[] = 
		{
			98, 88, 80, 65, 76, 45, 55, 70, 50, 55, 90, 45,
			100, 178, 76, 125, 110, 100, 101, 99, 112, 107			
		};
		for(double c : crashingArr){
			botNotCrashing.addCandle(new BpCandlestick(c));
		}
		assertFalse(botNotCrashing.isCrashing());
	}

	/*
	@Test
	void parseBalanceTest(){
		String jsStr = new String();
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("balancesSnapshotJSON.test").getFile());            
		try {
			jsStr = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Balance b = js.parseBalance(jsStr, Currency.XRP);
		assert(b).getAvailable().equals("0.976");
	}
	*/
}