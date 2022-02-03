package com.botpanda;

import com.botpanda.components.BotLogic;
import com.botpanda.components.BotSettings;
import com.botpanda.components.connection.BpConnectivity;
import com.botpanda.components.connection.BpJSONtemplates;
import com.botpanda.components.indicators.EngulfingPattern;
import com.botpanda.components.indicators.ExponentialMovingAverage;
import com.botpanda.entities.BpCandlestick;
import com.botpanda.entities.enums.Currency;
import com.botpanda.entities.enums.OrderSide;
import com.botpanda.entities.enums.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class BotpandaApplicationTests {
	@Autowired
	private BpConnectivity con;
	private static final BotLogic bl = new BotLogic();
	@Autowired
	private BpJSONtemplates js;
	private static BotSettings settings;

	@BeforeAll
	public static void init(){
		settings = new BotSettings();
		settings.setMaxCandles(20);
		settings.setStrategy(Strategy.MACD_RSI_EMA);
		bl.setSettings(settings);
		bl.rsi.setRsiLength(8);
		bl.ema.setEmaLength(5);
		double[] blArr = {100, 100, 110,125, 110, 100, 90, 80, 65};
		for(double c : blArr){
			bl.addCandle(new BpCandlestick(c));
		}
	}

	@Test
	void parseFromJsonTest() {
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
			assertEquals(bl.getCandleList().size(), settings.getMaxCandles() + 1);
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
		assertEquals(value, expectedvalue);
	}

	@Test
	void emaCalcTest(){
		//when
		bl.ema.setMaxEmaListLength(10);
		int expectedvalue = 84;
		double value = bl.ema.getLast();
		log.info("EMA list: " + bl.ema.getEmaList());
		log.info("EMA = " + value);
		assertEquals((int) value, expectedvalue);
	}

	@Test
	void simpleAvgTest(){
		double[] blArr = {100, 110, 120, 110};
		ArrayList<Double> l = new ArrayList<>();
		for(Double c : blArr){
			l.add(c);
		}
		assertEquals(110, (double) ExponentialMovingAverage.simpleAverage(l, 3));
	}

	@Test
	void engulfingPatternTest(){
		ArrayList<BpCandlestick> candles = new ArrayList<>();
		BpCandlestick small, big;
		small = new BpCandlestick();
		small.setClose(3);
		small.setHigh(5);
		small.setOpen(4);
		small.setLow(3);
		small.setVolume(100);

		big = new BpCandlestick();
		big.setClose(5);
		big.setHigh(6);
		big.setOpen(3);
		big.setLow(2);
		big.setVolume(200);

		candles.add(small);
		candles.add(big);

		EngulfingPattern engulf = new EngulfingPattern(candles);
		engulf.calc();

		assertTrue(engulf.shouldBuy());

		small.setVolume(250);
		small.setOpen(5);
		small.setClose(3);
		small.setLow(3);
		small.setHigh(5);
		candles.add(small);

		engulf.calc();
		assertTrue(engulf.shouldSell());

	}
}