package com.sample;

import io.undertow.Undertow;
import io.undertow.util.Headers;

/**
 * @author Zero
 *         Created on 2017/4/19.
 */
public class HelloWorld {
    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(exchange -> {
                    exchange.getResponseHeaders().put(Headers.SERVER, "Undertow Server");
                    exchange.getResponseSender().send("Hello World!");
                })
//                .setIoThreads(4)
//                .setWorkerThreads(16)
                .build();
        server.start();
    }
}
