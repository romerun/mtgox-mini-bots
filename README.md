Collection of MtGox hardcoded strategy trading bots.

------------

MiniCrashBot: Designed to run on a rally expected to crash a bit and continue to rise. User can configure to sell at a drop of X% from high, and buy back when it jumps Y% from lo.

Example:

$ java -cp mtgox-mini-bots-0.0.3.jar com.romerun.trading.mtgox.MiniCrashBot -apiKey "XXXXXX" -secretKey "YYYYY" \
  -sellTrigger "a:6:70" -sellTrigger "b:10:300" -buyTrigger "a:3:70" -buyTrigger "b:5:999"

Sell 70btc if price drop 6% from high, sell another 300btc if the drop continues to 10%,
Buy 70btc only if "a" was executed and price jumps 3% from low, buy another 999btc(or the maximum you can afford) only if b was executed and price jumps 5% from low.

* Currently it can only trade BTC/USD.
* It only fires such orders once. In the example, after it sends the 4 order above, it won't send any more orders, because it's designed for a single crash.
  After price is stablize, restart it for another crash.
* User can configure high and low with "-hi" "-lo" params, otherwise it will rely on the high and low from the trade data since the start of program.
* It fires Market order, so it's guaranteed to hit instantly unless gox is slow then only god knows what would happen.

--------
