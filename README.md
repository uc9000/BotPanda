# BotPanda
## Use of this bot at your own risk!
Crypto trading bot for BitPanda Pro made with Spring Boot
![alt text](https://github.com/uc9000/BotPanda/blob/main/images/screenshot_2.png?raw=true)

The bot sells or buys currencies at a given price limit based on common indicators (only RSI for now).
It retrieves and analyzes candles given from Bitpanda Pro REST and Web Sockets.

## To Do:
- [x] automatic reconnection on unexpected websocket close
- [x] Sending actual order requests to web socket (requires more testing first)
- [x] MACD indicator for trading
- [x] Full strategies using EMA, RSI and MACD
- [ ] ATR based stop loss   
- [ ] Simple GUI with use of HTML and REST
