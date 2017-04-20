package com.sample;

import com.sample.handler.BaseAuthHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class MetricsExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();
        router.get("/admin/*", exchange -> {
            exchange.getResponseSender().send(exchange.getRelativePath());
        });

        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        handler = new BaseAuthHandler(handler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(1)
                .build();
        server.start();
    }


}
