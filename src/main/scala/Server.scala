package main

import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

object Server {
    val handler = Undertow
        .builder()
        .addHttpListener(9001, "0.0.0.0")
        .setHandler(
            new HttpHandler {
                override def handleRequest(exchange: HttpServerExchange) = {
                    val code = exchange
                        .getQueryParameters
                        .get("code")
                        .toArray
                        .apply(0)
                        .toString
                        
                    Settings.update("authorization-code", code)
                    exchange.getResponseSender.send("")
                }
            }
        ).build

    def start = {
        println("Listening to auth code locally")
        handler.start
    }
}