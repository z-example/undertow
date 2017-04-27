package com.sample.ext.v1;

import com.sample.handler.FormDataHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;

/**
 * @author Zero
 *         Created on 2017/2/2.
 */
public class Example {
    //ab -n 100 -c 10 http://127.0.0.1:8080/hh
    public static void main(String[] args) throws Exception {
        //>ab -n 100000 -c 100 http://localhost:8080/name 68s


        RoutingHandler router = Handlers.routing();
        router.get("/", (RequestHandlerV1) (request, response) -> {
            response.end("Hello World!");
        });

        router.get("/test", RequestHandlerV1.adpt(Example::test));

        router.get("/hello", exchange -> {
            exchange.getResponseSender().send("Hello World!");
        });

        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        handler = new FormDataHandler(handler);//加上这句之后,就可以自动处理所有formdata了
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(handler)
                .setWorkerThreads(32)
//                .setIoThreads(4)
                .build();
        server.start();
    }

    public static void test(Request request, Response response) {

    }
}
