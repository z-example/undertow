package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.PathTemplateMatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class ThreadExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();

        router.get("test", exchange -> {
            System.out.println("---EventLoop Thread----> " + Thread.currentThread().getName());
            System.out.println("---1----> " + exchange.getDispatchExecutor());
            //切换到Word线程
            exchange.dispatch(() -> {
                System.out.println("---Worker Thread (dispatch)----> " + Thread.currentThread().getName());
                //切回到IO线程
                exchange.dispatch(exchange.getIoThread(), () -> {
                    System.out.println("---EventLoop Thread (dispatch)----> " + Thread.currentThread().getName());
                    exchange.getResponseSender().send("Hello World");
                });
            });
        });

        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(4)
                .build();
        server.start();
    }

}
