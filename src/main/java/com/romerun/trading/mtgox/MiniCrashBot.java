package com.romerun.trading.mtgox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.service.account.polling.PollingAccountService;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;
import com.xeiam.xchange.service.trade.polling.PollingTradeService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

final class MiniCrashBot {
  @Parameter(names = "-apiKey", description = "apiKey", required = true)
  private String apiKey;

  @Parameter(names = "-secretKey", description = "secretKey", required = true)
  private String secretKey;

  @Parameter(names = "-sellTrigger", description = "sellTrigger", required = true, variableArity = true, converter = TriggerConverter.class)
  private List<Trigger> sellTriggers = new ArrayList<Trigger>();
  
  @Parameter(names = "-buyTrigger", description = "buyTrigger", required = true, variableArity = true, converter = TriggerConverter.class)
  private List<Trigger> buyTriggers = new ArrayList<Trigger>();

  @Parameter(names = "-hi", description = "hi")
  private double hi = 1;
  
  @Parameter(names = "-lo", description = "lo")
  private double lo = 999999999;

	static Exchange getMyExchange(String apiKey, String secretKey) {
		ExchangeSpecification exchangeSpecification = new ExchangeSpecification("com.xeiam.xchange.mtgox.v1.MtGoxExchange");
		exchangeSpecification.setApiKey(apiKey);
		exchangeSpecification.setSecretKey(secretKey);
		exchangeSpecification.setUri("https://mtgox.com");
		exchangeSpecification.setHost("mtgox.com");
		return ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		MiniCrashBot conf = new MiniCrashBot();
		new JCommander(conf, args);

		double hi = conf.hi;
		double lo = conf.lo;
		Exchange mtGox = getMyExchange(conf.apiKey, conf.secretKey);

		// Get the account information

		PollingAccountService accountService = mtGox.getPollingAccountService();
		PollingMarketDataService marketDataService = mtGox.getPollingMarketDataService();
    PollingTradeService tradeService = mtGox.getPollingTradeService();

		AccountInfo accountInfo = accountService.getAccountInfo();
		System.out.println("AccountInfo as String: " + accountInfo.toString() + "\n");

		HashMap<String,Trigger> sellTriggers = new HashMap<String,Trigger>();
		for (Trigger t: conf.sellTriggers) {
		  sellTriggers.put(t.name, t);
		}
		
		for (;;) {
			Ticker ticker = marketDataService.getTicker(Currencies.BTC, Currencies.USD);
			double last = ticker.getLast().getAmount().doubleValue();

      double drop = (hi - last) / hi * 100;
      double jump = (last - lo) / lo * 100;
      
      System.out.println(String.format("hi %f lo %f last %f, drop %f percent jump, %f percent", hi, lo, last, drop, jump));

      for (Entry<String, Trigger> tuple : sellTriggers.entrySet()) {
        String triggerName = tuple.getKey();
        Trigger trigger = tuple.getValue();
        
        if (drop >= trigger.percent) {
          System.out.println("Below " + trigger.percent + "%");

          if (trigger.stopOrderId == null) {
            OrderType orderType = (OrderType.ASK);
            BigDecimal tradeableAmount = new BigDecimal(trigger.size);
            String tradableIdentifier = "BTC";
            String transactionCurrency = "USD";

            MarketOrder marketOrder = new MarketOrder(orderType, tradeableAmount, tradableIdentifier, transactionCurrency);
            trigger.stopOrderId = tradeService.placeMarketOrder(marketOrder);

            System.out.println("Selling off " + triggerName + ": " + trigger.stopOrderId);
          }
        }
      }

      for (Trigger trigger : conf.buyTriggers) {
        if (jump >= trigger.percent) {
          System.out.println("Above " + trigger.percent + "%");

          if (trigger.stopOrderId == null && sellTriggers.get(trigger.name).stopOrderId != null) {
            OrderType orderType = (OrderType.BID);
            BigDecimal tradeableAmount = new BigDecimal(trigger.size);
            String tradableIdentifier = "BTC";
            String transactionCurrency = "USD";

            MarketOrder marketOrder = new MarketOrder(orderType, tradeableAmount, tradableIdentifier, transactionCurrency);
            trigger.stopOrderId = tradeService.placeMarketOrder(marketOrder);

            System.out.println("Buying back: " + trigger.name + ": " + trigger.stopOrderId);
          }
        }
      }
      
      if (last > hi) {
        hi = last;
      }
      
      if (last < lo) {
        lo = last;
      }

			Thread.sleep(1000);
		}
	}
}
