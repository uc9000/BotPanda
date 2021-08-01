package com.botpanda;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.botpanda.components.BotLogic;
import com.botpanda.components.BotSettings;
import com.botpanda.components.BpConnectivity;
import com.botpanda.components.BpJSONtemplates;
import com.botpanda.components.indicators.ExponentialMovingAverage;
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
		settings.setMaxCandles(20);
		bl.setSettings(settings);
		bl.rsi.setRsiLength(8);
		bl.ema.setEmaLength(5);
		double blArr[] = {100, 100, 110,125, 110, 100, 90, 80, 65};
		for(double c : blArr){
			bl.addCandle(new BpCandlestick(c));
		}
	}

	@Test
	void parseFromJsonTest() throws InterruptedException {
		//given
		settings = new BotSettings();
		settings.setFromCurrency(Currency.BTC);
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
		log.info("value = " + value + " and expected = " + expectedvalue);
		log.info("RS = " + bl.rsi.getLastRs() + " ; avg loss = " + bl.rsi.getLastAvgLoss() + " and avg gain = " + bl.rsi.getLastAvgGain());
		//then
		assertTrue(value == expectedvalue);
	}

	@Test
	void emaCalcTest(){
		//when
		bl.ema.setMaxEmaListLength(10);
		int expectedvalue = 84;
		Double value =  bl.ema.getLast().doubleValue();
		//then
		// double blArr[] = {100, 100, 110,125, 110, 100, 90, 80, 65, 95, 29,46,6,3,54,656,32,455,66,22,33,55,66,33,99};
		// for(double c : blArr){
		// 	bl.addCandle(new BpCandlestick(c));
		// }
		log.info("EMA list: " + bl.ema.getEmaList());
		log.info("EMA = " + value);
		assertTrue((int)value.doubleValue() == expectedvalue);
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
	void simpleAvgTest(){
		double blArr[] = {100, 110, 120, 110};
		ArrayList<Double> l = new ArrayList<Double>();
		for(Double c : blArr){
			l.add(c);
		}
		assertTrue(ExponentialMovingAverage.simpleAverage(l, 3) == 110);
	}
}